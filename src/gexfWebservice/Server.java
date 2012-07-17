package gexfWebservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Server extends UnicastRemoteObject implements ServerInterface {
	private static String lastFileContent = "";
	private static String lastHashValue = "";
	private static final long serialVersionUID = 1L;
	
	Server() throws RemoteException
	{
		super();
		System.out.println("Server created...");
	}
	
    /**
     * The method creates the specified file with the given content
     * 
     * @param path to the file, that is going to be created
     * @param content the content of the file
     */
    private void createFile(String path, String content){
		try{
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
    }
    
    /**
     * This method reads the content of a given file
     * 
     * @param path the path to the file
     * @return the content of the file as a String
     */
    private String getContent(String path){
    	File tempfile = new File(path);
    	String contentOfFile ="";

    	try {
    		BufferedReader input =  new BufferedReader(new FileReader(tempfile));
    		try {
    			String line = null; 
    			while (( line = input.readLine()) != null){
    				contentOfFile += line;
    			}
    		}finally {
    			input.close();
    		}
    	}catch (Exception e){
    		e.printStackTrace();
    	}

    	return contentOfFile.toString();
    }
    
    /**
     * The method hashes the content of the given file and 
     * returns the SHA256 hash as a return value
     * 
     * @param path of the file that should be checked
     * @return the SHA256 hash
     */
    private String hashCodeSHA256(String path){
    	String content = getContent(path);
    	
    	if(content.equals(lastFileContent)){ // shortcut, maybe we know the output already ;-)
    		return lastHashValue;
    	}else{
    		/* else -> hash the content
    		 * note: the actual hashing code was taken from
    		 * http://www.mkyong.com/java/java-sha-hashing-example/
    		 */
    		MessageDigest md = null;
    		try {
    			md = MessageDigest.getInstance("SHA-256");
    		} catch (NoSuchAlgorithmException e) {
    			e.printStackTrace();
    		}
    		md.update(content.getBytes());

    		byte byteData[] = md.digest();

    		//convert the byte to hex format method 1
    		StringBuffer sb = new StringBuffer();
    		for (int i = 0; i < byteData.length; i++) {
    			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
    		}

    		// save shortcut values
    		lastFileContent = content;
    		lastHashValue = sb.toString();

    		// return hash value
    		return sb.toString();
    	}
    }
    
    /**
     * The method hashes the given string 
     * returns the SHA256 hash as a return value
     * 
     * @param string that is going to be hashed
     * @return the SHA256 hash
     */
    private String hashStringSHA256(String content){
    	/*
    	 * note: the actual hashing code was taken from
    	 * http://www.mkyong.com/java/java-sha-hashing-example/
    	 */
    	MessageDigest md = null;
    	try {
    		md = MessageDigest.getInstance("SHA-256");
    	} catch (NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}
    	md.update(content.getBytes());

    	byte byteData[] = md.digest();

    	//convert the byte to hex format method 1
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < byteData.length; i++) {
    		sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
    	}

    	// return hash value
    	return sb.toString();
    }

    /**
     * The methods checks if a given file exists. 
     * If the file does not exist, the method creates it and
     * inserts the content of the given file
     *  
     * @param path the file that has to be checked
     * @param content if the file has to be created, use
     * this string as content for the file
     */
    private void doesFileExist(String path, String stringcontent){
    	File f = new File(path);
    	if(f.exists()) {
    		/* do nothing, the file has already been hashed and saved */ 
    	}else{
    		createFile(path,stringcontent);
    	}
    }

	/**
	 * The method returns the path to the circos image, which is a local represantation of 'item' in
	 * the graph that is specified in 'path' 
	 * 
	 * @param path the path to the gexf file that should be used
	 * @param item the item which is the central part of this circos visualization
	 * @param sna the used metric for the circos ideogram (eg. "cc","dc","bc")
	 * @return a string that contains the path to the image 
	 */
	public String getLocalCircos(String path, String item, String sna) throws RemoteException {
		String hashname = hashCodeSHA256(path);
		
		CircosConfFile circconf = new CircosConfFile();
		CircosTuple tuple = new CircosTuple(null,null);
		Gephi gep = new Gephi();
		gep.fillCircos(path, sna, 1000, tuple); // TODO 1000 *doof* eher so max = -1 oder so...
		
		// clean nodes and edges
		tuple.getEdges().cleanEdgeListToOnlyEdgesFromOneNode(item);
		tuple.getNodes().cleanNodeListToEdges(tuple.getEdges());
		
		circconf.setNodes(tuple.getNodes());
		circconf.setEdges(tuple.getEdges());
		circconf.addParameter("image", "file", hashname+"_"+sna+"_"+item+".png");
		circconf.addParameter("image", "dir", Settings.CIRCOS_GFX_PREFIX);
		circconf.writeFile(hashname);
		
		String runCommand = Settings.CIRCOS_BIN_PREFIX+"circos -conf "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt";
		String resizeCommand = "convert "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+sna+"_"+item+".png" +
				" resize 20% "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+sna+"_"+item+"_small.png";
		try {
			Process p = Runtime.getRuntime().exec(runCommand);
			InputStreamReader instream = new InputStreamReader(p.getInputStream());
			BufferedReader in = new BufferedReader(instream);
			String line = null;
			while((line = in.readLine()) != null){
				System.out.println(line);
			}
			
			Process p2 = Runtime.getRuntime().exec(resizeCommand);
			InputStreamReader instream2 = new InputStreamReader(p2.getInputStream());
			BufferedReader in2 = new BufferedReader(instream2);
			line = null;
			while((line = in2.readLine()) != null){
				System.out.println(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "circos/gfx/"+hashname+"_"+sna+"_"+item+".png";
	}
	
	public static void main(String [] args){	
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		} 
		
		try {
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			System.out.println("created registry ...");
		}catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}
		
		try {
			Server myserv = new Server();
			LocateRegistry.getRegistry().rebind("Server", myserv);
			System.out.println("finished binding server name ...");
		}catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * This method returns an XML String which contains all sna metrics of the given gexf file
	 * 
	 * @param path The path to the gexf file, which should be used to calculate the metrics
	 * @return a xml string that contains all metrics
	 */
	public String getMetrics(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		String result = gephi.getMetrics(path);
		
		return result;
	}

	/**
	 * This method returns the top 'rank' bibliographic coupling edges of the graph, that is specified by 
	 * the given parameters
	 * 
	 * @param eventid of graph that should be used to generate the edges
	 * @param eventseriesid of graph that should be used to generate the edges
	 * @param syear the start year
	 * @param eyear the end year
	 * @param rank the number of edges that should be generated
	 * @return a string that contains a html table with the edges
	 */
	public String getBCEdges(String eventid,String eventseriesid,String syear,String eyear, int rank) throws RemoteException {
		BibliographicCouplingGraph ggraph = new BibliographicCouplingGraph(eventid, eventseriesid, syear, eyear);
		
		ggraph.connectToDB(Settings.DB_CONNECTOR, Settings.DB_USER, Settings.DB_PASSWORD);
		boolean init_finished = ggraph.init();
		if( init_finished ){
			ggraph.calculateGraph();
			TreeMap<Integer, ArrayList <String>> bib_coup_count = ggraph.getBib_coup_count();
			ggraph.close();
			
			String output = "<table class=\"zebra-striped\">\n\t<tr><th>name</th><th>bib value</th></tr>\n";
			
			int count = 1;
			boolean stop = false;
			for(int i = 0; i < rank; i++){
				Entry<Integer,ArrayList <String>> temp = bib_coup_count.pollLastEntry();
				
				if(temp != null && !stop){
					for(String entry : temp.getValue()){ // all entries for this value
						output += "\t<tr><td>"+ entry +"</td>" +
								"<td>"+temp.getKey()+"</td></tr>\n";
						
						if(count >= rank){
							stop = true;
							break;
						}
						count++;
					}
				}
			}
			output += "</table>";
			
			return output;
		}else{
			return "ERROR";
		}
	}
	
	/**
	 * The method returns a string of the format "nodes#edges", that contains the number of nodes and the number of edges
	 * of the graph, that is specified in the gexf file, which can be found at 'path'
	 * 
	 * @param path to the gexf file
	 * @return a string that contains the number of nodes and edges of the graph
	 */
	public String getNodesAndEdges(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		int nodes = gephi.getNodes(path);
		int edges = gephi.getEdges(path);
		
		return nodes+"#"+edges;
	}

	/**
	 * The method returns a double value which describes the density
	 * of the graph, that is specified in the gexf file, which can be found at 'path'
	 * 
	 * @param path to the gexf file
	 * @return a double value that contains the density of the graph
	 */
	public double getDensity(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		return gephi.getDensity(path);
	}

	/**
	 * The method returns the path to the circos image, which is representation of
	 * the graph that is specified in 'filename'. In addition to that the ideogram size is filled
	 * with the metric, that is specified in the parameter 'metric'. Furthermore, the 
	 * parameter rank specifies the number of items that should be used in the circos graph
	 * 
	 * @param filename the path to the gexf file that should be used
	 * @param metric the used metric for the circos ideogram (eg. "cc","dc","bc")
	 * @param rank specifies the number of items that should be used
	 * @return a string that contains the path to the image 
	 */
	public String getCircosPath(String filename, String metric, int rank) throws RemoteException {
		String hashname = hashCodeSHA256(filename);
		
		CircosConfFile circconf = new CircosConfFile();
		CircosTuple tuple = new CircosTuple(null,null);
		Gephi gep = new Gephi();
		gep.fillCircos(filename, metric, rank, tuple);
		CircosEdgeList circedges = tuple.getEdges();
		CircosNodeList circnodes = tuple.getNodes();
		
		circconf.setEdges(circedges);
		circconf.setNodes(circnodes);
		circconf.addParameter("image", "file", hashname+"_"+metric+"_"+rank+".png");
		circconf.addParameter("image", "dir", Settings.CIRCOS_GFX_PREFIX);
		circconf.writeFile(hashname);
		
		String runCommand = Settings.CIRCOS_BIN_PREFIX+"circos -conf "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt";
		String resizeCommand = "convert "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+metric+"_"+rank+".png" +
				" resize 20% "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+metric+"_"+rank+"_small.png";
		try {
			Process p = Runtime.getRuntime().exec(runCommand);
			InputStreamReader instream = new InputStreamReader(p.getInputStream());
			BufferedReader in = new BufferedReader(instream);
			String line = null;
			while((line = in.readLine()) != null){
				System.out.println(line);
			}
			
			Process p2 = Runtime.getRuntime().exec(resizeCommand);
			InputStreamReader instream2 = new InputStreamReader(p2.getInputStream());
			BufferedReader in2 = new BufferedReader(instream2);
			line = null;
			while((line = in2.readLine()) != null){
				System.out.println(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "circos/gfx/"+hashname+"_"+metric+"_"+rank+".png";
	}

	/**
	 * This method returns a path to a gexf file, that has been generated according to the given parameters
	 * 
	 * @param type the type of the graph that should be generated ("cc" for co-citation / "bc" for bibliographic coupling)
	 * @param eventid of graph that should be used to generate the edges
	 * @param eventseriesid of graph that should be used to generate the edges
	 * @param syear the start year
	 * @param eyear the end year
	 * @return a string that contains the path to the gexf file
	 */
	public String getGraphPath(String type, String eventid, String eventseriesid, String syear, String eyear) throws RemoteException {
		GephiGraph ggraph = null;
		
		switch(type){
		case "cc":
			ggraph = new CoCitationGraph(eventid, eventseriesid, syear, eyear);
			break;
		case "bc":
			ggraph = new BibliographicCouplingGraph(eventid, eventseriesid, syear, eyear);
			break;
		}
		
		ggraph.connectToDB(Settings.DB_CONNECTOR, Settings.DB_USER, Settings.DB_PASSWORD);
		boolean init_finished = ggraph.init();
		if( init_finished ){
			ggraph.calculateGraph();
			String graph = ggraph.getGexfGraph();
			String hashname = hashStringSHA256(graph);
			
			String filename = Settings.APACHE_PATH + "hash/" + hashname + ".gexf";
			doesFileExist(filename, graph);
			ggraph.close();
			return Settings.DOMAIN_PREFIX + hashname;
		}else{
			return Settings.DOMAIN_PREFIX + "ERROR";
		}
	}
}
