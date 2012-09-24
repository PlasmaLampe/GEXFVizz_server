package gexfWebservice;

/**
 * Each instance of this class represents a node within the graph (will all 
 * information, like SNA value, etc.) 
 * 
 * @author Jörg Amelunxen
 *
 */
public class MyNode{
	private String id;			// 'real id' of the graph file
	private int systemID;	// this id is gephi specific and is needed for computations
	private double value;
	private double standardizedValue;
	
	MyNode(String string, int systemID, double standardizedValue, double value){
		this.id = string;
		this.systemID = systemID;
		this.standardizedValue = standardizedValue;
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the systemID
	 */
	public int getSystemID() {
		return systemID;
	}

	/**
	 * @param systemID the systemID to set
	 */
	public void setSystemID(int systemID) {
		this.systemID = systemID;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the standardizedValue
	 */
	public double getStandardizedValue() {
		return standardizedValue;
	}

	/**
	 * @param standardizedValue the standardizedValue to set
	 */
	public void setStandardizedValue(double standardizedValue) {
		this.standardizedValue = standardizedValue;
	}
	
	
}

