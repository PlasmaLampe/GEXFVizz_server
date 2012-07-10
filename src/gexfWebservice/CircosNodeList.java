package gexfWebservice;

import java.util.HashSet;

public class CircosNodeList extends CircosList{
	private class CircosNode implements Comparable<CircosNode>{
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
	
	private HashSet<CircosNode> nodes;
	private int maxLabelLenght;
	
	public CircosNodeList() {
		nodes = new HashSet<CircosNode>();
		maxLabelLenght = 20;
	}
	
	public void addNode(String id, String label, Double size){
		String cleanlabel = this.createID(label);
		nodes.add(new CircosNode(id, cleanlabel, size));
	}
	
	/*public void cutAfterRank(int rank){
		TreeSet<CircosNode> tempnodes = new TreeSet<CircosNode>();
		
		if(rank < nodes.size()){
			for(int i=0; i < rank; i++){
				tempnodes.add(nodes.pollLast());
			}
			
			nodes = tempnodes;
		}	
	}
	
	public boolean containsNode(String id){
		for(CircosNode check : nodes){
			if(check.id == id)
				return true;
		}
		return false;
	}*/
	
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
