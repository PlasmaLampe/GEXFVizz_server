package gexfWebservice;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is the main server of the web application
 * 
 * @author Jörg Amelunxen
 *
 */
public class Server extends UnicastRemoteObject implements ServerInterface {
	private static String lastFileContent = "";
	private static String lastHashValue = "";
	private static final long serialVersionUID = 1L;
	
	public Server() throws RemoteException
	{
		super();
		System.out.println("Server created...");
	}
    
    /**
     * The method hashes the content of the given file and 
     * returns the SHA256 hash as a return value
     * 
     * @param path of the file that should be checked
     * @return the SHA256 hash
     */
    private String hashCodeSHA256(String path){
    	String content = Tools.getContent(path);
    	
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
	 * The method returns the path to the circos image, which is a local representation of 'item' in
	 * the graph that is specified in 'path' 
	 * 
	 * @param path the path to the gexf file that should be used
	 * @param item the item that is the central part of this circos visualization
	 * @param metric the used metric for the circos ideogram (eg. "cc","dc","bc")
	 * @return a string that contains the path to the image 
	 */
	public String getLocalCircos(String path, String item, String metric) throws RemoteException {
		String hashname = hashCodeSHA256(path);
		
		CircosConfFile circconf = new CircosConfFile(false);
		Gephi gep = new Gephi();
		
		CircosTuple tuple = gep.fillCircos(path, metric, 100000);
		Set<String> anodes = tuple.getAdjecentNodeIDs(item);
		
		gep = null;
		
		// clean nodes and edges
		tuple.getEdges().cleanEdgeListToOnlyEdgesFromOneNodeOrBetweenTwoAdjacentNodes(item, anodes);
		tuple.getNodes().cleanNodeListToEdges(tuple.getEdges());
		
		if(tuple.getNodes().getNodes().size() > 0){
			circconf.setNodes(tuple.getNodes());
			circconf.setEdges(tuple.getEdges());
			circconf.addParameter("image", "file", hashname+"_"+metric+"_"+item+".png");
			circconf.addParameter("image", "dir", Settings.CIRCOS_GFX_PREFIX);
			circconf.writeFile(hashname);

			String runCommand = Settings.CIRCOS_BIN_PREFIX+"circos -conf "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt";
			//String resizeCommand = "convert "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+sna+"_"+item+".png" +
			//		" resize 20% "+Settings.CIRCOS_GFX_PREFIX+hashname+"_"+sna+"_"+item+"_small.png";
			String zipCommand = "zip "+ Settings.CIRCOS_DATA_PREFIX+hashname +" "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt " + Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt " + 
					Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt " + Settings.CIRCOS_DATA_PREFIX+"ideogram.conf " + 
					Settings.CIRCOS_DATA_PREFIX+"ticks.conf "+ Settings.CIRCOS_DATA_PREFIX+"sna"+hashname+".txt "+
					Settings.CIRCOS_DATA_PREFIX+"gpy"+hashname+".txt ";
			
			try {
				Process p = Runtime.getRuntime().exec(runCommand);
				InputStreamReader instream = new InputStreamReader(p.getInputStream());
				BufferedReader in = new BufferedReader(instream);
				String line = null;
				while((line = in.readLine()) != null){
					System.out.println(line);
				}

				//Runtime.getRuntime().exec(resizeCommand);
				Runtime.getRuntime().exec(zipCommand);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "circos/gfx/"+hashname+"_"+metric+"_"+item+".png";
		}else
			return "error/gfx/zeroNodes.png";
		
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
	 * This method returns an XML String that contains all SNA metrics of the given gexf file
	 * 
	 * @param path The path to the gexf file, which should be used to calculate the metrics
	 * @return a XML string that contains all metrics
	 */
	public String getMetrics(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		String result = gephi.getMetrics(path);
		gephi = null;
		
		return result;
	}

	/**
	 * This method returns the top 'rank' bibliographic coupling edges of the graph that is specified by 
	 * the given parameters
	 * 
	 * @param eventid of graph that should be used to generate the edges
	 * @param eventseriesid of graph that should be used to generate the edges
	 * @param syear the start year
	 * @param eyear the end year
	 * @param rank the number of edges that should be generated
	 * @return a string that contains a HTML table with the edges
	 */
	public String getBCEdges(String eventid,String eventseriesid,String syear,String eyear, int rank) throws RemoteException {
		BibliographicCouplingGraph ggraph = new BibliographicCouplingGraph(eventid, eventseriesid, syear, eyear);
		
		ggraph.connectToDB(findCorrectDatabase(eventseriesid),Settings.DB_USER,Settings.DB_PASSWORD);
		
		boolean init_finished = ggraph.init();
		if( init_finished ){
			ggraph.calculateGraph();
			TreeMap<Integer, ArrayList <String>> bib_coup_count = ggraph.getBib_coup_count();
			String hash = this.hashStringSHA256(ggraph.getGexfGraph());
			ggraph.close();
			
			String output = "<table class=\"sortable\">\n\t<tr><th>name</th><th>bib value</th></tr>\n";
			
			int count = 1;
			boolean stop = false;
			for(int i = 0; i < rank; i++){
				Entry<Integer,ArrayList <String>> temp = bib_coup_count.pollLastEntry();
				
				if(temp != null && !stop){
					for(String entry : temp.getValue()){ // all entries for this value
						String [] labels = entry.split("###");
						String id1 = ggraph.getLabelToIDMap().get(labels[0].trim());
						String id2 = ggraph.getLabelToIDMap().get(labels[1].trim());
						
						String labellink = "<a href=\""+ Settings.TomcatURLToServlet +"id=" + hash + "&item=" +
								"" + id1 +"&metric=bc\">"+ labels[0] +"</a>" + " to <a href=\""+ Settings.TomcatURLToServlet +"id=" + 
								hash + "&item=" + "" + id2 +"&metric=bc\">"+ labels[1] +"</a>" + "";
						
						output += "\t<tr><td>"+ labellink +"</td>" +
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
	 * The method returns a string of the format "nodes#edges", which contains the number of nodes and the number of edges
	 * of the graph that is specified in the gexf file that can be found at 'path'
	 * 
	 * @param path to the gexf file
	 * @return a string that contains the number of nodes and edges of the graph
	 */
	public String getNodesAndEdges(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		int nodes = gephi.getNodes(path);
		int edges = gephi.getEdges(path);
		gephi = null;
		
		return nodes+"#"+edges;
	}

	/**
	 * The method returns a double value that describes the density
	 * of the graph, which is specified in the gexf file that can be found at 'path'
	 * 
	 * @param path to the gexf file
	 * @return a double value that contains the density of the graph
	 */
	public double getDensity(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		double returnvalue = gephi.getDensity(path);
		gephi = null;
		
		return returnvalue;
	}

	/**
	 * The method returns the path to the circos image, which is representation of
	 * the graph that is specified in 'filename'. In addition to that, the ideogram size is filled
	 * with the metric that is specified in the parameter 'metric'. Furthermore, the 
	 * parameter rank specifies the number of items that should be used in the circos graph
	 * 
	 * @param filename the path to the gexf file that should be used
	 * @param metric the used metric for the circos ideogram (eg. "cc","dc","bc")
	 * @param rank specifies the number of items that should be used
	 * @return a string that contains the path to the image 
	 */
	public String getCircosPath(String filename, String metric, int rank, boolean preview) throws RemoteException {
		String hashname = hashCodeSHA256(filename);
		
		CircosConfFile circconf = new CircosConfFile(preview);
		Gephi gep = new Gephi();
		CircosTuple tuple = gep.fillCircos(filename, metric, rank);
		gep=null;
		
		CircosEdgeList circedges = tuple.getEdges();
		CircosNodeList circnodes = tuple.getNodes();
		
		circconf.setEdges(circedges);
		circconf.setNodes(circnodes);
		circconf.addParameter("image", "file", hashname+"_"+metric+"_"+rank+".png");
		circconf.addParameter("image", "dir", Settings.CIRCOS_GFX_PREFIX);
		circconf.writeFile(hashname);
		
		String runCommand = Settings.CIRCOS_BIN_PREFIX+"circos -conf "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt";
		String zipCommand = "zip "+ Settings.CIRCOS_DATA_PREFIX+hashname +" "+Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt " + Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt " + 
				Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt " + Settings.CIRCOS_DATA_PREFIX+"ideogram.conf " + 
				Settings.CIRCOS_DATA_PREFIX+"ticks.conf "+ Settings.CIRCOS_DATA_PREFIX+"sna"+hashname+".txt "+
				Settings.CIRCOS_DATA_PREFIX+"gpy"+hashname+".txt ";
		try {
			Process p = Runtime.getRuntime().exec(runCommand);
			InputStreamReader instream = new InputStreamReader(p.getInputStream());
			BufferedReader in = new BufferedReader(instream);
			String line = null;
			while((line = in.readLine()) != null){
				System.out.println(line);
			}
			
			Runtime.getRuntime().exec(zipCommand);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "circos/gfx/"+hashname+"_"+metric+"_"+rank+".png";
	}

	/**
	 * This method returns a path to a gexf file that has been generated according to the given parameters
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
		
		ggraph.connectToDB(findCorrectDatabase(eventseriesid),Settings.DB_USER,Settings.DB_PASSWORD);
		
		boolean init_finished = ggraph.init();
		if( init_finished ){
			ggraph.calculateGraph();
			String graph = ggraph.getGexfGraph();
			String hashname = hashStringSHA256(graph);
			
			String filename = Settings.APACHE_PATH + "hash/" + hashname + ".gexf";
			Tools.doesFileExist(filename, graph);
			ggraph.close();
			return Settings.DOMAIN_PREFIX + hashname;
		}else{
			return Settings.DOMAIN_PREFIX + "ERROR";
		}
	}

	/**
	 * This method is used to find the correct MySQL database that contains the given 
	 * eventseriesid 
	 * 
	 * @param eventseriesid of the event that should be in the database
	 * @returns jdbc.odbc path to the database
	 */
	private String findCorrectDatabase(String eventseriesid) {
		System.out.println( "looking for correct database");
		String resultDatabase = "";
		boolean found = false;
		
		try {
			FileInputStream fstream = new FileInputStream(Settings.PATH_TO_DATABASES);
			DataInputStream dIn = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(dIn));
			
			String strLine;
			Connection con = null;

			while(((strLine = br.readLine())!=null && found == false)){
				try {
					try {
						Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); //Or any other driver
					}
					catch(Exception x){
						System.out.println( "Unable to load the driver class!");
					}

					con = DriverManager.getConnection(Settings.MYSQL_PREFIX+strLine, Settings.DB_USER, Settings.DB_PASSWORD);

					if(!con.isClosed())
						System.out.println("Successfully connected to " +
								"MySQL server using TCP/IP...");

				} catch(Exception e) {
					System.err.println("Exception: " + e.getMessage());
				} 
				
				// is this the correct database ?
				Statement teststmt = con.createStatement();
				ResultSet testrs = teststmt.executeQuery( "SELECT text FROM eventseries WHERE id=\""+eventseriesid+"\"");
				while( testrs.next() ){
					//there was an entry
					found = true;
					resultDatabase = Settings.MYSQL_PREFIX + strLine;
					
					System.out.println( "found: "+resultDatabase);
				}
				testrs.close();
				teststmt.close();
				con.close();
			}
			
			br.close();
			dIn.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return resultDatabase;
	}

	/**
	 * This method returns a path to a .gephi project file
	 * 
	 * @param hashPath the path to the gexf file that should be included in this project
	 * @return a string that contains the path to the .gephi file
	 */
	public String getPathToProject(String hashPath) throws RemoteException {
		Gephi gephi = new Gephi();	
		String returnvalue = gephi.getProject(hashPath);
		gephi = null;
		
		return returnvalue;
	}
}
