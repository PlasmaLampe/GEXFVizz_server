/**
 * 
 */
package gexfWebservice;

import java.util.HashSet;

/**
 * This small helper class stores the nodes and edges of a Circos visualization
 * 
 * @author Jörg Amelunxen
 *
 */
public class CircosTuple {
	private CircosEdgeList edges;
	private CircosNodeList nodes;
	
	/**
	 * @param edges
	 * @param nodes
	 */
	public CircosTuple(CircosEdgeList edges, CircosNodeList nodes) {
		this.edges = edges;
		this.nodes = nodes;
	}

	/**
	 * @return the edges
	 */
	public CircosEdgeList getEdges() {
		return edges;
	}

	/**
	 * @param edges the edges to set
	 */
	public void setEdges(CircosEdgeList edges) {
		this.edges = edges;
	}

	/**
	 * @return the nodes
	 */
	public CircosNodeList getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(CircosNodeList nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * This method returns all id's of adjacent nodes of the given node
	 * @param item the "main" node
	 * @return a String HashSet with the id's of all adjacent nodes
	 */
	public HashSet<String> getAdjecentNodeIDs(String item){
		HashSet<String> anodes = new HashSet<String>();
		
		for(CircosEdge edge : edges.getEdges()){
			if(edge.getFrom().equals(item)){
				anodes.add(edge.getTo());
			}else if(edge.getTo().equals(item)){
				anodes.add(edge.getFrom());
			}
		}
		return anodes;
	}
}
