package gexfWebservice;

import java.util.ArrayList;

public class CircosEdgeList extends CircosList{
	private class CircosEdge{
		private String id;
		private String from;
		private String to;
		private float pos;
		
		public CircosEdge(String id, String from, String to, float pos) {
			super();
			this.id = id;
			this.from = from;
			this.to = to;
			this.pos = pos;
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
		 * @return the position
		 */
		public float getPos() {
			return pos;
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
	
	public CircosEdgeList() {
		edges = new ArrayList<CircosEdge>();
	}
	
	public void addEdge(String from, String to, float pos){
		edges.add(new CircosEdge(from+"#"+to, from, to, pos));
	}

	@Override
	public void writeFile(String hashname) {
		int count = 0;
		for(CircosEdge edge : edges){
			output += edge.getID()+"#"+count + " " + edge.getFrom() + " 0 " + (int) (edge.getPos() * 100) + "\n";
			output += edge.getID()+"#"+count + " " + edge.getTo() + " 0 " + (int) (edge.getPos() * 100) + "\n";
			count++;
		}
		
		createFile(Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt", output);
	}
}
