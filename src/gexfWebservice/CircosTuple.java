/**
 * 
 */
package gexfWebservice;

import java.util.HashSet;

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
