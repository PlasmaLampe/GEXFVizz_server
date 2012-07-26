package gexfWebservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class CircosNode implements Comparable<CircosNode>{
	private String id;
	private String label;
	private double snaMetricValue;
	private double szMetricValue;
	
	public CircosNode(String id, String label, double snaMetricValue, double szMetricValue) {
		this.id = id;
		this.label = label;
		this.snaMetricValue = snaMetricValue;
		this.szMetricValue = szMetricValue;
	}

	public String getID() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public Double getSnaMetricValue() {
		return snaMetricValue;
	}

	public int compareTo(CircosNode o) {
		return (int)((o.getSnaMetricValue() * 1000) - (this.getSnaMetricValue() * 1000));
	}

	/**
	 * @return the szMetricValue
	 */
	public double getSzMetricValue() {
		return szMetricValue;
	}
	
	
}

public class CircosNodeList extends CircosList{
	private ArrayList<CircosNode> nodes;
	private int maxLabelLenght;
	private HashMap<String,String> colors;
	
	public CircosNodeList() {
		nodes = new ArrayList<CircosNode>();
		maxLabelLenght = 20;
		colors = new HashMap<String,String>();
	}
	
	public void addNode(String id, String label, double sna, double sz){
		String cleanlabel = this.createID(label);
		nodes.add(new CircosNode(id, cleanlabel, sna, sz));
		
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
			output += "chr - " + node.getID() + " " + useThisLabel + " 0 " + Math.round(node.getSzMetricValue()) + " " + node.getID() + " \n";
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
