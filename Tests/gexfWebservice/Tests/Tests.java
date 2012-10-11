package gexfWebservice.Tests;

import static org.junit.Assert.*;

import java.rmi.RemoteException;

import gexfWebservice.*;

import org.junit.Test;

public class Tests {	
	/* check every method of the public server RMI-interface */
	@Test public void TestgetPathToProject(){
		try{
			Server serv = new Server();
			String result = serv.getPathToProject(Settings.TESTPATH + "bc_test_ectel0612.gexf");
			String expected = Settings.HTTPIP + "data/bc_test_ectel0612.gephi";
			assertTrue(result.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getGraphPathTestBC(){
		try {
			Server serv = new Server();
			serv.getGraphPath("bc", "02a48acb-fedc-474c-85f9-a76ba0066599", "2006", "2012");
			String resultContent = Tools.getContent(Settings.APACHE_PATH + "/hash/a62c288ef85e87c00bf6893454e7a97b1d7ae05651c8ad4907f7817ae9fa015b.gexf");
			String expected = Tools.getContent(Settings.TESTPATH + "bc_test_ectel0612.gexf");
			assertTrue(resultContent.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getGraphPathTestCC(){
		try {
			Server serv = new Server();
			serv.getGraphPath("cc", "02a48acb-fedc-474c-85f9-a76ba0066599", "2006", "2012");
			String resultContent = Tools.getContent(Settings.APACHE_PATH + "/hash/a62c288ef85e87c00bf6893454e7a97b1d7ae05651c8ad4907f7817ae9fa015b.gexf");
			String expected = Tools.getContent(Settings.TESTPATH + "cc_test_ectel0612.gexf");
			assertTrue(resultContent.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getCircosPathTestBC(){
		try{
			Server serv = new Server();
			String result = serv.getCircosPath(Settings.TESTPATH + "bc_test_ectel0612.gexf", "bc", 10, true);
			String expected = "circos/gfx/2b845e704f33800606ac72a604dd2ebdd66d50eb667575cef0ff3d8b1db29a0b_bc_10.png";
			assertTrue(expected.equals(result));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getCircosPathTestCC(){
		try{
			Server serv = new Server();
			String result = serv.getCircosPath(Settings.TESTPATH + "bc_test_ectel0612.gexf", "cc", 10, true);
			String expected = "circos/gfx/2b845e704f33800606ac72a604dd2ebdd66d50eb667575cef0ff3d8b1db29a0b_cc_10.png";
			assertTrue(expected.equals(result));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getMetricsTest(){
		try{
			Server serv = new Server();
			Tools.createFile(Settings.TESTPATH + "temp.tmp", serv.getMetrics(Settings.TESTPATH + "bc_test_ectel0612.gexf"));
			String resultXML = Tools.getContent(Settings.TESTPATH + "temp.tmp");
			String expected = Tools.getContent(Settings.TESTPATH + "metricTest.txt");
			assertTrue(resultXML.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getLocalCircosTest(){
		try{
			Server serv = new Server();
			String result = serv.getLocalCircos(Settings.TESTPATH + "bc_test_ectel0612.gexf", "34ee9ffb-d691-4ac5-9349-3274206fe8d2", "cc");
			String expected = "circos/gfx/2b845e704f33800606ac72a604dd2ebdd66d50eb667575cef0ff3d8b1db29a0b_cc_34ee9ffb-d691-4ac5-9349-3274206fe8d2.png";
			assertTrue(result.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getBCEdgesTest(){
		try{
			Server serv = new Server();
			Tools.createFile(Settings.TESTPATH + "temp.tmp", serv.getBCEdges("02a48acb-fedc-474c-85f9-a76ba0066599", "2006", "2012", 10000));
			String result = Tools.getContent(Settings.TESTPATH + "temp.tmp");
			String expected = Tools.getContent(Settings.TESTPATH + "bcEdgesTest.txt");
			assertTrue(result.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getNodesAndEdgesTest(){
		try{
			Server serv = new Server();
			String result = serv.getNodesAndEdges(Settings.TESTPATH + "bc_test_ectel0612.gexf");
			String expected = "271#795";
			assertTrue(result.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getDensityTest(){
		try{
			Server serv = new Server();
			Double result = serv.getDensity(Settings.TESTPATH + "bc_test_ectel0612.gexf");
			Double expected = 0.021730217302173022;
			assertTrue(result.equals(expected));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}