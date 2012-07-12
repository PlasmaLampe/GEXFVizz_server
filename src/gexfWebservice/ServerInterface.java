package gexfWebservice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote
{
	public String getGraphPath(String type, String eventid, String eventseriesid, String syear, String eyear) throws RemoteException;
	public String getCircosPath(String filename, String metric, int rank) throws RemoteException;
	public String getMetrics(String path) throws RemoteException;
	public String getBCEdges(String eventid, String eventseriesid, String syear, String eyear, int rank) throws RemoteException;
	public String getNodesAndEdges(String path) throws RemoteException;
	public double getDensity(String path) throws RemoteException;
}