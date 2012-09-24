package gexfWebservice;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class is needed to acquire the date information of the GEXF file.
 * This feature is implemented in the Gephi Toolkit, but was hardly usable. Thus,
 * we had to implement our own "YearExtractor" 
 * 
 * @author Jörg Amelunxen
 *
 */
public class GephiYearExtractor implements ContentHandler {
	private class YearTuple{
		private String start;
		private String end;
		
		/**
		 * @param start
		 * @param end
		 */
		public YearTuple(String start, String end) {
			this.start = start;
			this.end = end;
		}

		/**
		 * @return the start
		 */
		public String getStart() {
			return start;
		}

		/**
		 * @return the end
		 */
		public String getEnd() {
			return end;
		}
	}
	
	private HashMap<String,YearTuple> dates;
	private int globalStart;
	private int globalEnd;
	
	/** 
	 * This method returns the start/spawn date of the given node
	 * @param id of the node that should be checked
	 * @return the start date
	 */
	String getStartOfID(String id){
		return dates.get(id).getStart();
	}
	
	/** 
	 * This method returns the end/de-spawn date of the given node
	 * @param id of the node that should be checked
	 * @return the end date
	 */
	String getEndOfID(String id){
		return dates.get(id).getEnd();
	}
	
	GephiYearExtractor(){
		dates = new HashMap<String,YearTuple>();
		globalStart = -1;
		globalEnd = -1;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {}

	@Override
	public void setDocumentLocator(Locator arg0) {}

	@Override
	public void skippedEntity(String arg0) throws SAXException {}

	@Override
	public void startDocument() throws SAXException {}

	@Override
	public void startElement(String uri, String localName, String qName,
		      Attributes atts) throws SAXException {
		
	    if (localName.equals("node")) {
		    String start = atts.getValue("", "start");
		    String end = atts.getValue("", "end");
		    String id = atts.getValue("", "id");
		    
		    if(globalStart == -1 || globalEnd == -1)
		    	initGlobalDates(start, end);
		    
		    updateGlobalDates(start, end);
		    
		    dates.put(id, new YearTuple(start,end));
		}
	}
	
	/**
	 * This method returns a set of integer values that contain all years within the global timeframe, which are currently missing
	 * @param datesSet of all currently used years (edges ...)
	 * @return an integer HashSet of all missing years
	 */
	public HashSet<Integer> getSetOfMissingDates(Set<String> datesSet){
		HashSet<Integer> result = new HashSet<Integer>();
		
		int frame = globalEnd - globalStart;
		
		for(int i = globalStart; i <= globalStart + frame; i++){
			if(datesSet.contains(""+i)){
				// date has been used, so it's fine
			}else{ // date is missing...
				result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * This method updates the global years
	 * 
	 * @param start year of the current node
	 * @param end year of the current node
	 */
	private void updateGlobalDates(String start, String end) {
		if(start != null){
			if(Integer.parseInt(start) < globalStart){
				globalStart = Integer.parseInt(start); 
			}
		}
		if(start != null){
			if(Integer.parseInt(start) > globalEnd){
				globalEnd = Integer.parseInt(start); 
			}
		}
		if(end != null){
			if(Integer.parseInt(end) > globalEnd){
				globalEnd = Integer.parseInt(end); 
			}
		}
	}

	/**
	 * This method initializes the global years
	 * 
	 * @param start year of the current node
	 * @param end year of the current node
	 */
	private void initGlobalDates(String start, String end) {
		if(globalStart == -1 && start != null){
			globalStart = Integer.parseInt(start);
		}
		if(globalEnd == -1 && end != null){
			globalEnd = Integer.parseInt(end);
		}
	}

	
	/**
	 * @return the globalStart
	 */
	public int getGlobalStart() {
		return globalStart;
	}

	/**
	 * @return the globalEnd
	 */
	public int getGlobalEnd() {
		return globalEnd;
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {}
}