package gexfWebservice;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

public class CircosConfFile {
	private String karyotype;
	private CircosEdgeList edges;
	private CircosNodeList nodes;
	
	private HashMap<String,String> linkparameter;
	private HashMap<String,String> imageparameter;
	
	private String output;
	private boolean useTicks;
	private boolean useIdeogram;
	
    /**
     * The method creates the specified file with the given content
     * @param path to the file, that is going to be created
     * @param content the content of the file
     */
    private void createFile(String path, String content){
		try{
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
    }
    
    /**
     * This methods adds a parameter with the given value to the named target map
     * @param targetMap e.g. "link","image"
     * @param parameter
     * @param value
     */
    public void addParameter(String targetMap, String parameter,String value){
    	switch(targetMap){
    	case "image":
    		if(imageparameter.containsKey(parameter)){
    			imageparameter.remove(parameter);
    			imageparameter.put(parameter, value);
    		}else{
    			imageparameter.put(parameter, value);
    		}
    		break;
    	case "link":
    		if(linkparameter.containsKey(parameter)){
    			linkparameter.remove(parameter);
    			linkparameter.put(parameter, value);
    		}else{
    			linkparameter.put(parameter, value);
    		}
    		break;
    	}
    }
    
	CircosConfFile(){
		karyotype = "data/karyotype.human.txt";
		
		linkparameter = new HashMap<String,String>();
		imageparameter = new HashMap<String,String>();
		linkparameter.put("file", "data/links2.txt"); // default value
		linkparameter.put("radius", "0.95r"); // default value
		linkparameter.put("bezier_radius", "0.1r"); // default value
		linkparameter.put("color", "black_a4"); // default value
		linkparameter.put("thickness", "3"); // default value
		linkparameter.put("ribbon", "yes"); // default value
		
		imageparameter.put("dir","."); // default value
		imageparameter.put("file","circos.png"); // default value
		imageparameter.put("png","yes"); // default value
		imageparameter.put("svg","no"); // default value
		imageparameter.put("radius","2500p"); // default value
		imageparameter.put("angle_offset","-90"); // default value
		imageparameter.put("auto_alpha_colors","yes"); // default value
		imageparameter.put("auto_alpha_steps","5"); // default value
		imageparameter.put("background","white"); // default value
		
		useIdeogram = true;
		useTicks = false;
		edges = null;
		nodes = null;
	}
	
	
	/**
	 * @param edges the edges to set
	 */
	public void setEdges(CircosEdgeList edges) {
		this.edges = edges;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(CircosNodeList nodes) {
		this.nodes = nodes;
	}

	public void writeFile(String hashname){
		// write node and edge file
		nodes.writeFile(hashname);
		edges.writeFile(hashname);
		
		// update data in configuration file
		karyotype = Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt";
		addParameter("link", "file", Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt");
		
		// write configuration file
		output = "karyotype = " + karyotype + "\n<links>\n\t<link>\n\t\t";
		
		for(String item : linkparameter.keySet()){ // write all link parameter
			output += item + " = " + linkparameter.get(item) + "\n\t\t";
		}
		
		output += "\n\t</link>\n</links>\n";
		
		if(useIdeogram){
			output += "<<include ideogram.conf>>\n";
		}
		
		if(useTicks){
			output += "<<include ticks.conf>>\n";
		}
		
		output += "<image>\n\t";
		for(String item : imageparameter.keySet()){ // write all image parameter
			output += item + " = " + imageparameter.get(item) + "\n\t";
		}			
		output += "\n</image>\n" +
				"\n<<include etc/colors_fonts_patterns.conf>>\n<<include etc/housekeeping.conf>>\n<<include etc/colors.conf>>";
		
		createFile(Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt", output);
	}		

}
