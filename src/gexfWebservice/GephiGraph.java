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
import java.util.TreeMap;


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
	 * 
	 * @returns true if load was successful
	 */
	public boolean init() {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = null;

			if(eventseriesid == null){
				// get a specific eventid
				rs = stmt.executeQuery( "SELECT publication_id FROM pub_evt WHERE event_id =\""+eventid+"\"");
			}else if(eventseriesid != null && syear == null && eyear == null){
				// get a complete eventseries - there were no dates entered 
				rs = stmt.executeQuery("SELECT pub.publication_id,pub.event_id FROM pub_evt pub WHERE EXISTS" +
						" (SELECT * FROM evt_evs evt WHERE evt.event_id=pub.event_id AND " +
						"evt.eventseries_id=\""+ eventseriesid +"\")");
			}else if(eventseriesid != null && syear != null && eyear != null && isNumb(syear) && isNumb(eyear)){
				// get a part of an eventseries - build up the query ... 
				// first: get the name of the eventseries
				Statement namestmt = con.createStatement();
				ResultSet rsname = namestmt.executeQuery( "SELECT filepath FROM eventseries WHERE id =\""+eventseriesid+"\"" );
				String conferenceName = "";
				while( rsname.next() ){
					conferenceName = rsname.getString(1);
				}
				rsname.close();
				namestmt.close();
				
				// now use the years to calculate the conferences
				int tsyear = Integer.parseInt(syear);

				String confstring = "";
				String idconfstring = "";
				
				while( tsyear <= Integer.parseInt(eyear) ){  // generate list of possible conferences
					confstring += "\""+conferenceName+"/"+tsyear+"\",";
					for(int i = 0; i < Settings.MAX_AMOUNT_OF_CONFERENCE_ISSUES;i++){
						confstring += "\""+conferenceName+"/"+tsyear+"/"+"Issue"+i+"\",";
					}
					tsyear++;
				}
				
				// now: load the conference ids
				Statement idstmt = con.createStatement();
				String idQuery = "SELECT id FROM event WHERE filepath IN (" + confstring.substring(0, confstring.length()-1) + ")";
				ResultSet rsid = idstmt.executeQuery( idQuery );
				if(Settings.DEBUG){
					System.out.println(" started idquery: " + idQuery);
				}
				while( rsid.next() ){
					idconfstring += "\""+rsid.getString(1)+"\",";
				}
				idconfstring = idconfstring.substring(0,idconfstring.length()-1);
				
				if(Settings.DEBUG){
					System.out.println("returned: " + idconfstring);
				}
				
				rsid.close();
				idstmt.close();
				
				String genQuery = "SELECT pub.publication_id, pub.event_id FROM pub_evt pub " +
						"WHERE pub.event_id IN (" + idconfstring + ")";					
				
				if(Settings.DEBUG){
					System.out.println(" started genquery: "+genQuery);
				}
				
				rs = stmt.executeQuery( genQuery );
			}else{
				return false;
			}
			
			while( rs.next() ){
				String idOfPublication = rs.getString(1);
				ArrayList<String> getCitedBy = new ArrayList<String>();
				ArrayList<String> cites = new ArrayList<String>();

				// get the publication that cite this one
				Statement innerstmt = con.createStatement();
				ResultSet innerrs = innerstmt.executeQuery( "SELECT publication1_id FROM citation " +
						"WHERE publication2_id=\"" + idOfPublication + "\"");
				while( innerrs.next() ){
					getCitedBy.add(innerrs.getString(1));
				}
				innerrs.close();
				innerstmt.close();
				
				// get the publications that are cited by this one
				Statement innerstmt2 = con.createStatement();
				ResultSet innerrs2 = innerstmt2.executeQuery( "SELECT publication2_id FROM citation " +
						"WHERE publication1_id=\"" + idOfPublication + "\"");
				while( innerrs2.next() ){
					cites.add(innerrs2.getString(1));
				}
				innerrs2.close();
				innerstmt2.close();
				
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
				
				// get the publishing year of this publication
				String year = "";
				Statement yearstmt = con.createStatement();
				ResultSet yearrs = yearstmt.executeQuery( "SELECT ev.text, ev.id FROM event ev WHERE EXISTS ( SELECT * FROM pub_evt pevt WHERE pevt.publication_id=\""+ idOfPublication +"\" && pevt.event_id=ev.id )");
				while( yearrs.next() ){
					year = yearrs.getString(1);
				}
				
				String [] yearArray = year.split(" ");
				int cleanyear = Integer.parseInt(yearArray[1].trim());

				yearrs.close();
				yearstmt.close();
				
				// store all of it
				setOfPublicationIDs.add(idOfPublication);
				mapOfPublications.put(idOfPublication, new Publication(idOfPublication, cleanyear, title, cites, getCitedBy));
			}
			rs.close() ;
			// Close the result set, statement and the connection
			stmt.close() ;
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	public HashMap<String, Publication> getMapOfPublications() {
		return mapOfPublications;
	}

	public Set<String> getSetOfPublicationIDs() {
		return setOfPublicationIDs;
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
	 * @param to is the pass to the database e.g. aan.cs.upb.de
	 * @param username
	 * @param password
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
		
		// generate Co-Citation:
		// algorithm says to paper that are cited by this one, that they are
		// cited together by this one ... ;-)
		for(String pub : setOfPublicationIDs){
			ArrayList<String> cites = mapOfPublications.get(pub).getCites();
			
			for(String citedPaper : cites){
				for(String othercitedPaper : cites){
					if(!othercitedPaper.equals(citedPaper)){
						Publication tpub = mapOfPublications.get(citedPaper);
						Publication tpub2 = mapOfPublications.get(othercitedPaper);
						if(tpub != null && tpub2 != null){
							tpub.addPaperThatHasBeenCitedWithThisOne(othercitedPaper);
						}else{
							System.out.println("Error: one publication was not found on this conference");
						}
					}
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
				gexfGraph += " end=\""+eyear+"\"";
			}
		}		 
		
		gexfGraph += ">\n\t\t<nodes>\n";
		
		// write nodes
		for(String publication : setOfPublicationIDs){
			String clearlabel = mapOfPublications.get(publication).getTitle();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "").replaceAll("&", " and ");
			
			gexfGraph += "\t\t\t<node id=\""+ mapOfPublications.get(publication).getId() +"\" " +
					"label=\""+ clearedlabel +"\" start=\""+ mapOfPublications.get(publication).getPublishedInYear() +"\"></node>\n";
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
						"\" target=\""+ paper +"\" weight=\""+ citedMap.get(paper) +"\"></edge>\n";
						
				countEdges++;
			}
		}
		gexfGraph += "\t\t</edges>\n\t</graph>\n</gexf>";
	}	
}

class BibliographicCouplingGraph extends GephiGraph{
	private TreeMap<Integer, ArrayList<String>> bib_coup_count; // this car is needed for the tabular visulization of the edges
	private HashMap<String,String> labelToIDMap;
	public BibliographicCouplingGraph(String eventid, String eventseriesid, String syear,
			String eyear) {
		super(eventid, eventseriesid, syear, eyear);
		
		bib_coup_count = new TreeMap<Integer, ArrayList<String>>();
		labelToIDMap = new HashMap<String,String>();
	}

	public void addEntryToBibCoup(String fromName, String toName, int value){
		if(!bib_coup_count.containsKey(value)){
			bib_coup_count.put(value, new ArrayList<String>());
		}
		bib_coup_count.get(value).add(fromName+" ### "+toName);
	}
	
	/**
	 * @return the bib_coup_count
	 */
	public TreeMap<Integer, ArrayList<String>> getBib_coup_count() {
		return bib_coup_count;
	}

	
	/**
	 * @return the labelToIDMap
	 */
	public HashMap<String, String> getLabelToIDMap() {
		return labelToIDMap;
	}

	@Override
	public void calculateGraph() {
		super.calculateGraph();
		
		// generate bibliographic coupling:
		for(String pub : setOfPublicationIDs){
			try {
				Statement idstmt = con.createStatement();		
				String idQuery = "SELECT publication2_id,count FROM bib_coupling WHERE publication1_id=" +
						"\"" + pub + "\"";
				ResultSet rsid = idstmt.executeQuery( idQuery );
				while( rsid.next() ){
					// is the other publication part of the choosen events ?
					String pubid = rsid.getString(1);
					int value = Integer.parseInt(rsid.getString(2));
					Publication checkPub = mapOfPublications.get(pubid);
					if(checkPub != null){
						this.addEntryToBibCoup(mapOfPublications.get(pub).getTitle(), checkPub.getTitle(), value); // store sorted edges
						mapOfPublications.get(pub).addBibliographicCouplingTo(pubid, value);
					}
				}
				rsid.close();
				idstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				gexfGraph += " end=\""+eyear+"\"";
			}
		}		 
		
		gexfGraph += ">\n\t\t<nodes>\n";
		
		// write nodes
		for(String publication : setOfPublicationIDs){
			String clearlabel = mapOfPublications.get(publication).getTitle();
			String clearedlabel = "NULL";
			if(clearlabel != null){
				clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "").replaceAll("&", " and ");
			}
			
			gexfGraph += "\t\t\t<node id=\""+ mapOfPublications.get(publication).getId() +"\" " +
					"label=\""+ clearedlabel +"\" start=\""+ mapOfPublications.get(publication).getPublishedInYear() +"\"></node>\n";
			
			// store label <-> id
			this.labelToIDMap.put(clearedlabel, publication);
		}		
		
		// write edges
		gexfGraph += "\t\t</nodes>\n\t\t<edges>\n";
		int countEdges = 0;
		for(String publication : setOfPublicationIDs){
			// get a publication
			HashMap<String, Integer> bc = mapOfPublications.get(publication).getBibliograpiccoupling();
			
			// look at the 'bibliograpic coupling' map and create edges
			for(String paper : bc.keySet()){
				gexfGraph += "\t\t\t<edge id=\""+ countEdges +"\" source=\""+ publication +
						"\" target=\""+ paper +"\" weight=\""+ bc.get(paper) +"\"></edge>\n";
						
				countEdges++;
			}
		}
		gexfGraph += "\t\t</edges>\n\t</graph>\n</gexf>";
	}	
}