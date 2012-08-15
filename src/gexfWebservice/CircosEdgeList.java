package gexfWebservice;

import java.util.ArrayList;
import java.util.Set;

class CircosEdge{
	private String id;
	private String from;
	private String to;
	private float weight;
	private double offsetStart;
	private double offsetEnd;
	
	public CircosEdge(String id, String from, String to, float weight, double offsetStart, double offsetEnd) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.weight = weight;
		this.offsetStart = offsetStart;
		this.offsetEnd = offsetEnd;
	}

	/**
	 * @return the id
	 */
	public String getID() {
		return id;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @return the weight
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * @return the offset of the start node
	 */
	public double getOffsetStartNode() {
		return offsetStart;
	}
	
	/**
	 * @return the offset of the end node
	 */
	public double getOffsetEndNode() {
		return offsetEnd;
	}
}

public class CircosEdgeList extends CircosList{
	private ArrayList<CircosEdge> edges;
	
	public void cleanEdgeList(CircosNodeList nodes){
		ArrayList<CircosEdge> cleanedList = new ArrayList<CircosEdge>();
		
		for(CircosEdge check : edges){
			if(nodes.containsNode(check.getFrom()) && nodes.containsNode(check.getTo())){
				// this is fine, source and target are valid nodes
				cleanedList.add(check);
			}else{
				// do not use this one
			}
		}
		
		edges = cleanedList;
	}
	
	public void cleanEdgeListToOnlyEdgesFromOneNode(String nodeID){
		ArrayList<CircosEdge> cleanedList = new ArrayList<CircosEdge>();
		
		for(CircosEdge check : edges){
			if(check.getFrom().equals(nodeID) || check.getTo().equals(nodeID)){
				// this is fine, source and target are valid nodes
				cleanedList.add(check);
			}else{
				// do not use this one
			}
		}
		
		edges = cleanedList; 
	}
	
	public void cleanEdgeListToOnlyEdgesFromOneNodeOrBetweenTwoAdjacentNodes(String nodeID, Set<String> adjacentNodes){
		ArrayList<CircosEdge> cleanedList = new ArrayList<CircosEdge>();
		
		for(CircosEdge check : edges){
			if(check.getFrom().equals(nodeID) || check.getTo().equals(nodeID)){
				// this is fine, source and target are valid nodes
				cleanedList.add(check);
			}else if(adjacentNodes.contains(check.getFrom()) && adjacentNodes.contains(check.getTo())){
				// this is also fine, source and target are valid nodes
				cleanedList.add(check);
			}else{
				// do not use this one
			}
		}
		
		edges = cleanedList; 
	}
	
	public boolean containsNodeAsSourceOrTarget(String NodeID){
		boolean containsNode = false;
		
		for(CircosEdge localedge : edges){
			if(localedge.getFrom().equals(NodeID) || localedge.getTo().equals(NodeID)){
				containsNode = true;
				break;
			}
		}
		
		return containsNode;
	}
	
	public CircosEdgeList() {
		edges = new ArrayList<CircosEdge>();
	}
	
	public void addEdge(String from, String to, float weight, double offsetStartNode, double OffsetEndNode){
		edges.add(new CircosEdge(from+"#"+to, from, to, weight, offsetStartNode, OffsetEndNode));
	}

	
	
	/**
	 * @return the edges
	 */
	public ArrayList<CircosEdge> getEdges() {
		return edges;
	}

	@Override
	public void writeFile(String hashname) {
		int count = 0;
		for(CircosEdge edge : edges){
			output += edge.getID()+"#"+count + Settings.CIRCOS_DELIMITER + edge.getFrom() + Settings.CIRCOS_DELIMITER + 
					(int) edge.getOffsetStartNode() + Settings.CIRCOS_DELIMITER + (int) (edge.getOffsetStartNode() + edge.getWeight()) + "\n";
			output += edge.getID()+"#"+count + Settings.CIRCOS_DELIMITER + edge.getTo() + Settings.CIRCOS_DELIMITER + 
					(int) edge.getOffsetEndNode() + Settings.CIRCOS_DELIMITER + (int) (edge.getOffsetEndNode() + edge.getWeight()) + "\n";
			count++;
		}
		
		createFile(Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt", output);
	}
}
