package gexfWebservice;

import java.util.ArrayList;

public class CircosEdgeList extends CircosList{
	private class CircosEdge{
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
	
	private ArrayList<CircosEdge> edges;
	
	public void cleanEdgeList(CircosNodeList nodes){
		ArrayList<CircosEdge> cleanedList = new ArrayList<CircosEdge>();
		
		for(CircosEdge check : edges){
			if(nodes.containsNode(check.from) && nodes.containsNode(check.to)){
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
			if(check.from.equals(nodeID) || check.to.equals(nodeID)){
				// this is fine, source and target are valid nodes
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
			if(localedge.from.equals(NodeID) || localedge.to.equals(NodeID)){
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

	@Override
	public void writeFile(String hashname) {
		int count = 0;
		for(CircosEdge edge : edges){
			output += edge.getID()+"#"+count + " " + edge.getFrom() + " " + (int) edge.getOffsetStartNode() + " " + (int) (edge.getOffsetStartNode() + edge.getWeight()) + "\n";
			output += edge.getID()+"#"+count + " " + edge.getTo() + " " + (int) edge.getOffsetEndNode() + " " + (int) (edge.getOffsetEndNode() + edge.getWeight()) + "\n";
			count++;
		}
		
		createFile(Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt", output);
	}
}
