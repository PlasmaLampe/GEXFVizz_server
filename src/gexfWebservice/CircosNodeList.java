package gexfWebservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Every instance of this class represents a node within the 
 * Circos visualization 
 * 
 * @author Jörg Amelunxen
 *
 */
class CircosNode implements Comparable<CircosNode>{
	private String id;
	private String label;
	private double snaMetricValue;
	private double szMetricValue;
	private HashMap<String,Integer> growthsPerYear;
	private String start;
	private String end;
	
	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(String start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(String end) {
		this.end = end;
	}

	public CircosNode(String id, String label, double snaMetricValue, double szMetricValue, HashMap<String,Integer> growthsPerYear) {
		this.id = id;
		this.label = label;
		this.snaMetricValue = snaMetricValue;
		this.szMetricValue = szMetricValue;
		
		this.growthsPerYear = growthsPerYear;
	}

	public String getID() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public double getSnaMetricValue() {
		return snaMetricValue;
	}

	public int compareTo(CircosNode o) {
		return (int)((o.getSzMetricValue() * 1000) - (this.getSzMetricValue() * 1000));
	}

	/**
	 * @return the szMetricValue
	 */
	public double getSzMetricValue() {
		return szMetricValue;
	}

	/**
	 * @return the growthsPerYear
	 */
	public HashMap<String, Integer> getGrowthsPerYear() {
		return growthsPerYear;
	}
}

/** 
 * This class contains all nodes of the Circos visualization
 * 
 * @author Jörg Amelunxen
 *  
 */
public class CircosNodeList extends CircosList{
	private ArrayList<CircosNode> nodes;
	private int maxLabelLenght;
	private HashMap<String,String> colors;
	
	public CircosNodeList() {
		nodes = new ArrayList<CircosNode>();
		maxLabelLenght = 30;
		colors = new HashMap<String,String>();
	}
	
	/**
	 * This method adds a node to the node list
	 * 
	 * @param id the id of the node that should be added 
	 * @param label the label of the node that should be added 
	 * @param sna the sna value of the node that should be added 
	 * @param sz the szientometric value of the node that should be added 
	 * @param growthsPerYear a HashMap that contains the years as keys and the amount of added edges in this year as values 
	 * 
	 */
	public void addNode(String id, String label, double sna, double sz, HashMap<String,Integer> growthsPerYear){
		nodes.add(new CircosNode(id, label, sna, sz, growthsPerYear));
		
		String color = Math.round(Math.random() * 255) + "," + 
				Math.round(Math.random() * 255) + "," + Math.round(Math.random() * 255); 
		colors.put(id, color);
	}
	
	/**
	 * This method deletes all nodes that are not attached to one of the given edges
	 * @param edges the edges, which should be checked
	 */
	public void cleanNodeListToEdges(CircosEdgeList edges){
		ArrayList<CircosNode> tempnodes = new ArrayList<CircosNode>();
		HashMap<String,String> tempcolors = new HashMap<String,String>();
		
		for(int i = 0; i < nodes.size(); i++){
			if(edges.containsNodeAsSourceOrTarget(nodes.get(i).getID())){
				tempnodes.add(nodes.get(i));
				tempcolors.put(nodes.get(i).getID(), colors.get(nodes.get(i).getID()));
			}
		}
			
		nodes = tempnodes;
		colors = tempcolors;
	}
	
	/**
	 * This methods deletes all nodes that are not under the @param RANK szientometric values 
	 * @param rank the amount of nodes, which should not be deleted
	 */
	public void cutAfterRank(int rank){
		ArrayList<CircosNode> tempnodes = new ArrayList<CircosNode>();
		HashMap<String,String> tempcolors = new HashMap<String,String>();
		Collections.sort(nodes);
		
		if(rank < nodes.size()){
			for(int i=0; i < rank; i++){
				tempnodes.add(nodes.get(i));
				tempcolors.put(nodes.get(i).getID(), colors.get(nodes.get(i).getID()));
			}
			
			nodes = tempnodes;
			colors = tempcolors;
		}	
	}
	
	/**
	 * This method checks if the given node is contained within the nodelist
	 * @param id of the searched node
	 * @return true, if the node is contained in the nodelist
	 */
	public boolean containsNode(String id){
		for(CircosNode check : nodes){
			if(check.getID() == id)
				return true;
		}
		return false;
	}
	
	/**
	 * This method returns the maximal growths in one year of the node
	 * @return the maximal growths
	 */
	public int maxGrowths(){
		int max = -1;
		for(CircosNode tempNode : nodes){
			for(int entry : tempNode.getGrowthsPerYear().values()){
				if(entry > max){
					max = entry;
				}
			}
		}
		return max;
	}
	
	/**
	 * @return the colors of the nodes
	 */
	public HashMap<String, String> getColors() {
		return colors;
	}

	/**
	 * This method returns the node with the given id
	 * @param id the id of the node that should be returned
	 * @return the CircosNode, if the node is not contained within the nodelist, the method returns null
	 */
	public CircosNode getNode(String id){
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).getID().equals(id)){
				return nodes.get(i);
			}
		}
		return null;
	}
	
	/**
	 * This method writes the node file
	 * 
	 * @param hashname is used to build the filename
	 */
	@Override
	public void writeFile(String hashname) {
		for(CircosNode node : nodes){
			String useThisLabel = "";
			if(node.getLabel().length() > maxLabelLenght){
				useThisLabel = node.getLabel().substring(0, maxLabelLenght) + "...";
			}else{
				useThisLabel = node.getLabel();
			}
			output += "chr" + Settings.CIRCOS_DELIMITER + "-" + Settings.CIRCOS_DELIMITER + node.getID() + Settings.CIRCOS_DELIMITER + useThisLabel + 
					Settings.CIRCOS_DELIMITER + "0" + Settings.CIRCOS_DELIMITER + Math.round(node.getSzMetricValue()) + 
					Settings.CIRCOS_DELIMITER + node.getID() + "\n";
		}
		Tools.createFile(Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt", output);
	}

	/**
	 * This method writes the node file without whitespaces. This file will be used to run the orderchr script of Circos
	 * 
	 * @param hashname is used to build the filename
	 */
	public void writeFileForCircosOrder(String hashname) {
		String dummyOut = "";
		for(CircosNode node : nodes){
			String useThisLabel = "dummyLabel";
			
			dummyOut += "chr" + Settings.CIRCOS_DELIMITER + "-" + Settings.CIRCOS_DELIMITER + node.getID() + Settings.CIRCOS_DELIMITER + useThisLabel + 
					Settings.CIRCOS_DELIMITER + "0" + Settings.CIRCOS_DELIMITER + Math.round(node.getSzMetricValue()) + 
					Settings.CIRCOS_DELIMITER + node.getID() + "\n";
		}
		Tools.createFile(Settings.CIRCOS_DATA_PREFIX+"nodeForCircosOrder"+hashname+".txt", dummyOut);
	}
	
	/**
	 * @return the nodes
	 */
	public ArrayList<CircosNode> getNodes() {
		return nodes;
	}
}
