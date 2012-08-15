package gexfWebservice.Tests;

import static org.junit.Assert.*;

import java.rmi.RemoteException;

import gexfWebservice.*;
import org.junit.Test;

public class Tests {
	@Test public void testLocalCircos(){
		//Server serv = new Server;
		//serv.getLocalCircos("data/e77b1839e8f031e3b6ead6dfe067757d948269d77a2f50f6ff2fcb6858d6ef0e.gexf", item, sna)
	}
	
	@Test public void testGenerateCircos() throws RemoteException{
		Server serv = new Server();
		String result = serv.getCircosPath(Settings.APACHE_PATH + "data/e77b1839e8f031e3b6ead6dfe067757d948269d77a2f50f6ff2fcb6858d6ef0e.gexf", "cc", 10);
		String expected = "circos/gfx/1af0b2e56edd8bd30effc4ea8eeca098b3e7c96791d054bb4435d30f064584bf_cc_10.png";
		assertTrue(expected.equals(result));
	}
}