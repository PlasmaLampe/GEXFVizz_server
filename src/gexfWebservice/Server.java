package gexfWebservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Server extends UnicastRemoteObject implements ServerInterface {
	
    /**
     * The method creates the specified file with the given content
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
     * The method hashes the given string 
     * returns the SHA256 hash as a return value
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
    		// TODO Auto-generated catch block
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
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Server() throws RemoteException
	{
		super();
		System.out.println("Server created...");
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

	@Override
	public String getMetrics(String path) throws RemoteException {
		// new stuff
		Gephi gephi = new Gephi();
		String result = gephi.getMetrics(path);
		
		return result;
	}

	@Override
	public String getNodesAndEdges(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		int nodes = gephi.getNodes(path);
		int edges = gephi.getEdges(path);
		
		return nodes+"#"+edges;
	}

	@Override
	public double getDensity(String path) throws RemoteException {
		Gephi gephi = new Gephi();
		return gephi.getDensity(path);
	}

	@Override
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
