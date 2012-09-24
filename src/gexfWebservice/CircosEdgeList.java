package gexfWebservice;

import java.util.ArrayList;
import java.util.Set;

/**
 * Every instance of this class represents a link within the 
 * Circos visualization 
 * 
 * @author Jörg Amelunxen
 *
 */
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
	 * @return the id of the source node
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the the id of the target node
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
	 * @return the offset of the source node
	 */
	public double getOffsetStartNode() {
		return offsetStart;
	}
	
	/**
	 * @return the offset of the target node
	 */
	public double getOffsetEndNode() {
		return offsetEnd;
	}
}

/**
 * This class contains all edges of the visualization
 * 
 * @author Jörg Amelunxen
 *
 */
public class CircosEdgeList extends CircosList{
	private ArrayList<CircosEdge> edges;
	
	/**
	 * Delete every edge that is not attached to one of the given nodes
	 * 
	 * @param nodes This nodelist contains all nodes that should be checked
	 */
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
	
	/**
	 * Delete every edge that is not attached to the given node
	 * 
	 * @param nodes This id is the id of the node that should be checked
	 */
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
	
	/**
	 * Delete every edge that is not attached to the given node or attached to two adjacent nodes
	 * 
	 * @param nodeID the id of the "main" node
	 * @param adjacentNodes a set with id's of all adjacent nodes
	 */
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
	
	/**
	 * Checks if the id of a node is used on an edge within the visualization 
	 * as source or target node
	 * 
	 * @param NodeID the id of the node
	 * @return true, if the id is used on an edge
	 */
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
	
	/**
	 * this methods adds an edge to the edgelist
	 * 
	 * @param from the id of the source node
	 * @param to the id of the target node
	 * @param weight the weight of the edge
	 * @param offsetStartNode the offset of the edge at the karyotype at the source node
	 * @param OffsetEndNode the offset of the edge at the karyotype at the target node
	 */
	public void addEdge(String from, String to, float weight, double offsetStartNode, double OffsetEndNode){
		edges.add(new CircosEdge(from+"#"+to, from, to, weight, offsetStartNode, OffsetEndNode));
	}

	/**
	 * @return the edges
	 */
	public ArrayList<CircosEdge> getEdges() {
		return edges;
	}

	/**
	 * This method writes the configuration file
	 * 
	 * @param hashname is used to build the filename
	 */
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
		
		Tools.createFile(Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt", output);
	}
}
