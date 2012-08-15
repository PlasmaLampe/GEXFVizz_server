package gexfWebservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

public class CircosNodeList extends CircosList{
	private ArrayList<CircosNode> nodes;
	private int maxLabelLenght;
	private HashMap<String,String> colors;
	
	public CircosNodeList() {
		nodes = new ArrayList<CircosNode>();
		maxLabelLenght = 30;
		colors = new HashMap<String,String>();
	}
	
	public void addNode(String id, String label, double sna, double sz, HashMap<String,Integer> growthsPerYear){
		String cleanlabel = this.createID(label);
		nodes.add(new CircosNode(id, cleanlabel, sna, sz, growthsPerYear));
		
		String color = Math.round(Math.random() * 255) + "," + 
				Math.round(Math.random() * 255) + "," + Math.round(Math.random() * 255); 
		colors.put(id, color);
	}
	
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
	
	public boolean containsNode(String id){
		for(CircosNode check : nodes){
			if(check.getID() == id)
				return true;
		}
		return false;
	}
	
	/**
	 * @return the colors
	 */
	public HashMap<String, String> getColors() {
		return colors;
	}

	public CircosNode getNode(String id){
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).getID().equals(id)){
				return nodes.get(i);
			}
		}
		return null;
	}
	
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
		createFile(Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt", output);
	}

	/**
	 * @return the nodes
	 */
	public ArrayList<CircosNode> getNodes() {
		return nodes;
	}
	
	
}
