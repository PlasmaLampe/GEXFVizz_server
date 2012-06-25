package gexfWebservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GephiGraph {
	protected Connection con;
	protected HashMap<String, Publication> mapOfPublications;
	protected Set<String> setOfPublicationIDs;
	protected String gexfGraph;
	
	protected String eventid;
	protected String eventseriesid;
	protected String syear;
	protected String eyear;
	
	public GephiGraph(String eventid, String eventseriesid, String syear, String eyear) {
		con = null;
		gexfGraph = "";
		mapOfPublications = new HashMap<String, Publication>();
		setOfPublicationIDs = new HashSet<String>();
		
		this.eventid = eventid;
		this.eventseriesid = eventseriesid;
		this.syear = syear;
		this.eyear = eyear;
	}

	protected boolean isNumb(String str){
		String s=str;
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * This method uses the MYSQL connection to fill an internal 
	 * HashMap and an internal Set with publications.
	 * 
	 * You can use the calculateGraph method AFTER this method 
	 * has been called.
	 */
	public void init() {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = null;

			if(eventseriesid == null){
				rs = stmt.executeQuery( "SELECT publication_id FROM pub_evt WHERE event_id =\""+eventid+"\"");
			}else{
				rs = stmt.executeQuery("SELECT pub.publication_id,pub.event_id FROM pub_evt pub WHERE EXISTS" +
						" (SELECT * FROM evt_evs evt WHERE evt.event_id=pub.event_id AND " +
						"evt.eventseries_id=\""+ eventseriesid +"\")");
			}
			
			while( rs.next() ){
				String idOfPublication = rs.getString(1);
				ArrayList<String> cites = new ArrayList<String>();
				
				Statement innerstmt = con.createStatement();
				ResultSet innerrs = innerstmt.executeQuery( "SELECT publication1_id FROM citation " +
						"WHERE publication2_id=\"" + idOfPublication + "\"");
				while( innerrs.next() ){
					cites.add(innerrs.getString(1));
				}
				innerrs.close();
				innerstmt.close();
				
				// get title of this publication
				String title = "";
				Statement labelstmt = con.createStatement();
				ResultSet labelrs = labelstmt.executeQuery( "SELECT title FROM publication " +
						"WHERE id=\""+ idOfPublication +"\"");
				while( labelrs.next() ){
					title = labelrs.getString(1);
				}
				labelrs.close();
				labelstmt.close();
				
				// store all of it
				setOfPublicationIDs.add(idOfPublication);
				mapOfPublications.put(idOfPublication, new Publication(idOfPublication, title, cites));
			}
			rs.close() ;
			// Close the result set, statement and the connection
			stmt.close() ;
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @return the graph
	 */
	public String getGexfGraph() {
		return gexfGraph;
	}

	/**
	 * This method creates the Graph and saves it in an internal variable.
	 */
	public void calculateGraph(){}
	
	/**
	 * This method establishes the connection to the given MYSQL DB.
	 * 
	 * @param to
	 * @param user
	 * @param pass
	 */
	public void connectToDB(String to, String user, String pass){
		try {
			try {
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); //Or any other driver
			}
			catch(Exception x){
				System.out.println( "Unable to load the driver class!");
			}

			con = DriverManager.getConnection(to, user, pass);

			if(!con.isClosed())
				System.out.println("Successfully connected to " +
						"MySQL server using TCP/IP...");

		} catch(Exception e) {
			System.err.println("Exception: " + e.getMessage());
		} 
			  
			
	}

	/**
	 * This method closes the connection to the MYSQL DB
	 */
	public void close() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class CoCitationGraph extends GephiGraph{
	
	public CoCitationGraph(String eventid, String eventseriesid, String syear,
			String eyear) {
		super(eventid, eventseriesid, syear, eyear);
	}

	@Override
	public void calculateGraph() {
		super.calculateGraph();
		
		// generate Co-Citation
		for(String pub : setOfPublicationIDs){
			ArrayList<String> cites = mapOfPublications.get(pub).getCites();
			
			for(String citedPaper : cites){
				for(String othercitedPaper : cites){
					if(!othercitedPaper.equals(citedPaper))
						mapOfPublications.get(citedPaper).addPaperThatHasBeenCitedWithThisOne(othercitedPaper);
				}
			}
		}
		
		// Create GEXF file
		gexfGraph = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"\t<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n" +
				"\t\t<graph mode=\"dynamic\" defaultedgetype=\"undirected\" timeformat=\"date\" ";
		
		if(syear != null){
			if(this.isNumb(syear)){
				gexfGraph += "start=\""+syear+"\"";
			}
		}
		if(eyear != null){
			if(this.isNumb(eyear)){
				gexfGraph += "end=\""+eyear+"\"";
			}
		}		 
		
		gexfGraph += ">\n\t\t<nodes>\n";
		
		// write nodes
		for(String publication : setOfPublicationIDs){
			String clearlabel = mapOfPublications.get(publication).getTitle();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			gexfGraph += "\t\t\t<node id=\""+ mapOfPublications.get(publication).getId() +"\" " +
					"label=\""+ clearedlabel +"\"></node>\n";
		}		
		
		// write edges
		gexfGraph += "\t\t</nodes>\n\t\t<edges>\n";
		int countEdges = 0;
		for(String publication : setOfPublicationIDs){
			// get a publication
			HashMap<String, Integer> citedMap = mapOfPublications.get(publication).getCitedTogetherWith();
			
			// look at the 'citedTogether' map and create edges
			for(String paper : citedMap.keySet()){
				gexfGraph += "\t\t\t<edge id=\""+ countEdges +"\" source=\""+ publication +
						"\" target=\""+ paper +"\" weight="+ citedMap.get(paper) +"\"></edge>\n";
						
				countEdges++;
			}
		}
		gexfGraph += "\t\t</edges>\n\t</graph>\n</gexf>";
	}
	
}