package gexfWebservice;

import java.util.ArrayList;
import java.util.Collections;

class CircosNode implements Comparable<CircosNode>{
	private String id;
	private String label;
	private Double size;
	
	public CircosNode(String id, String label, Double size) {
		this.id = id;
		this.label = label;
		this.size = size;
	}

	public String getID() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public Double getSize() {
		return size;
	}

	public int compareTo(CircosNode o) {
		return (int)((o.getSize() * 1000) - (this.getSize() * 1000));
	}
}

public class CircosNodeList extends CircosList{
	private ArrayList<CircosNode> nodes;
	private int maxLabelLenght;
	
	public CircosNodeList() {
		nodes = new ArrayList<CircosNode>();
		maxLabelLenght = 20;
	}
	
	public void addNode(String id, String label, Double size){
		String cleanlabel = this.createID(label);
		nodes.add(new CircosNode(id, cleanlabel, size));
	}
	
	public void cleanNodeListToEdges(CircosEdgeList edges){
		ArrayList<CircosNode> tempnodes = new ArrayList<CircosNode>();
		Collections.sort(nodes);
		
		for(int i = 0; i < nodes.size(); i++){
			if(edges.containsNodeAsSourceOrTarget(nodes.get(i).getID())){
				tempnodes.add(nodes.get(i));
			}
		}
			
		nodes = tempnodes;
	}
	
	public void cutAfterRank(int rank){
		ArrayList<CircosNode> tempnodes = new ArrayList<CircosNode>();
		Collections.sort(nodes);
		
		if(rank < nodes.size()){
			for(int i=0; i < rank; i++){
				tempnodes.add(nodes.get(i));
			}
			
			nodes = tempnodes;
		}	
	}
	
	public boolean containsNode(String id){
		for(CircosNode check : nodes){
			if(check.getID() == id)
				return true;
		}
		return false;
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
			output += "chr - " + node.getID() + " " + useThisLabel + " 0 " + (int) (node.getSize() * 100) + "\n";
		}
		
		createFile(Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt", output);
	}
	
}
