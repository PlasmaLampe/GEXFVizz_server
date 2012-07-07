/**
 * 
 */
package gexfWebservice;

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
	
	
}
