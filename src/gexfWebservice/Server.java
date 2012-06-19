package gexfWebservice;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements ServerInterface {
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

}
