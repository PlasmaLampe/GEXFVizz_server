package gexfWebservice;

import java.util.HashSet;

public class CircosNodeList extends CircosList{
	private class CircosNode{
		private String id;
		private String label;
		private float size;
		
		public CircosNode(String id, String label, float size) {
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
		
		public float getSize() {
			return size;
		}
	}
	
	private HashSet<CircosNode> nodes;
	private int maxLabelLenght;
	
	public CircosNodeList() {
		nodes = new HashSet<CircosNode>();
		maxLabelLenght = 20;
	}
	
	public void addNode(String id, String label, float size){
		String cleanlabel = this.createID(label);
		nodes.add(new CircosNode(id, cleanlabel, size));
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
