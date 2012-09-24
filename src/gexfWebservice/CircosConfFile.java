package gexfWebservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * This class represents the Circos main configuration file
 *  
 * @author Jörg Amelunxen
 *
 */
public class CircosConfFile {
	private String karyotype;
	private CircosEdgeList edges;
	private CircosNodeList nodes;
	private boolean preview;
	
	private HashMap<String,String> linkparameter;
	private HashMap<String,String> imageparameter;
	private HashMap<String,String> snaplotparameter;
	private HashMap<String,String> colors;
	
	private String output;
	private boolean useTicks;
	private boolean useIdeogram;
	private HashMap<String, String> snabackgroundtparameter;
	private HashMap<String, String> snaaxesparameter;
	private HashMap<String, String> gpyparameter;
    
    /**
     * This methods adds a parameter with the given value to the named target map
     * @param targetMap e.g. "link","image", sna
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
    	case "sna":
    		if(snaplotparameter.containsKey(parameter)){
    			snaplotparameter.remove(parameter);
    			snaplotparameter.put(parameter, value);
    		}else{
    			snaplotparameter.put(parameter, value);
    		}
    		break;
    	case "gpy":
    		if(gpyparameter.containsKey(parameter)){
    			gpyparameter.remove(parameter);
    			gpyparameter.put(parameter, value);
    		}else{
    			gpyparameter.put(parameter, value);
    		}
    		break;
    	}
    }
    
	CircosConfFile(boolean preview){
		karyotype = "";
		this.preview = preview;
		
		linkparameter = new HashMap<String,String>();
		snaplotparameter = new HashMap<String,String>();
		snabackgroundtparameter = new HashMap<String,String>();
		snaaxesparameter = new HashMap<String,String>();
		snaplotparameter = new HashMap<String,String>();
		imageparameter = new HashMap<String,String>();
		gpyparameter = new HashMap<String,String>();
		
		colors = new HashMap<String,String>();
		colors.put("chr", "0,0,0");
		
		linkparameter.put("file", "data/links2.txt"); // default value
		linkparameter.put("radius", "0.7r"); // default value
		linkparameter.put("bezier_radius", "0.1r"); // default value
		linkparameter.put("color", "black_a4"); // default value
		linkparameter.put("thickness", "3"); // default value
		linkparameter.put("ribbon", "yes"); // default value
		linkparameter.put("flat", "yes");
		
		imageparameter.put("dir","."); // default value
		imageparameter.put("file","circos.png"); // default value
		imageparameter.put("png","yes"); // default value
		imageparameter.put("svg","no"); // default value
		imageparameter.put("radius","2500p"); // default value
		imageparameter.put("angle_offset","-90"); // default value
		imageparameter.put("auto_alpha_colors","yes"); // default value
		imageparameter.put("auto_alpha_steps","5"); // default value
		imageparameter.put("background","white"); // default value
		
		snaplotparameter.put("show", "yes");
		snaplotparameter.put("type", "histogram");
		snaplotparameter.put("r0", "0.75r");
		snaplotparameter.put("r1", "0.85r");
		snaplotparameter.put("min", "0");
		snaplotparameter.put("max", "15");
		snaplotparameter.put("color", "black");
		snaplotparameter.put("fill_under", "yes");
		snaplotparameter.put("fill_color", "red");
		snaplotparameter.put("thickness", "2");
		snaplotparameter.put("extend_bin", "no");
		
		gpyparameter.put("show", "yes");
		gpyparameter.put("type", "histogram");
		gpyparameter.put("r0", "0.87r");
		gpyparameter.put("r1", "0.97r");
		gpyparameter.put("min", "0");
		gpyparameter.put("max", "10");
		gpyparameter.put("color", "black");
		gpyparameter.put("fill_under", "yes");
		gpyparameter.put("fill_color", "blue");
		gpyparameter.put("thickness", "2");
		gpyparameter.put("extend_bin", "no");
		
		snaaxesparameter.put("color", "black_a4"); // also used for gpyparameter
		snaaxesparameter.put("thickness", "1"); // also used for gpyparameter
		
		snabackgroundtparameter.put("color", "vlgrey"); // also used for gpyparameter
		
		useIdeogram = true;
		useTicks = true;
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

	/**
	 * This methods writes the Circos configuration file(-s) to the path
	 * specified within the Settings class
	 * @param hashname will be used to build the filenames
	 */
	public void writeFile(String hashname){	
		// write node and edge file
		nodes.writeFile(hashname);
		edges.writeFile(hashname);
		
		// run orderChr script
		String outputOrder="";
		
		if(!preview){
			nodes.writeFileForCircosOrder(hashname);
			
			if(Settings.DEBUG){
				System.out.println("running Circos order script ...");
			}
			
			try {
				String runCommand = Settings.CIRCOS_DATA_ORDERCHR+"bin/orderchr -links "+Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt"+
						" -karyotype "+Settings.CIRCOS_DATA_PREFIX+"nodeForCircosOrder"+hashname+".txt";
				
				Process p = Runtime.getRuntime().exec(runCommand);
				InputStreamReader instream = new InputStreamReader(p.getInputStream());
				BufferedReader in = new BufferedReader(instream);
				String line = null;
				while((line = in.readLine()) != null){
					System.out.println(line);
					
					if(line.startsWith("chromosomes_order"))
						outputOrder+=line;
				}		
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(Settings.DEBUG){
				System.out.println("order: "+outputOrder);
			}
		}
		
		// write sna (histogram)
		writeSnaHistogram(nodes, hashname);
		
		// write GrowthsPerYear (histogram)
		this.addParameter("gpy", "max", ""+nodes.maxGrowths());
		writeGrowthsPerYearHistogram(nodes, hashname);
		
		// get colors 
		colors = nodes.getColors();
		
		// update data in configuration file
		karyotype = Settings.CIRCOS_DATA_PREFIX+"node"+hashname+".txt";
		addParameter("link", "file", Settings.CIRCOS_DATA_PREFIX+"edge"+hashname+".txt");
		
		// write configuration file
		output = "file_delim = " + Settings.CIRCOS_PRINT_DELIMITER + "\n";
		output += "karyotype = " + karyotype + "\n";
		output += outputOrder+"\n<colors>\n\t\t";
		
		for(String item : colors.keySet()){ // write all link parameter
			output += item + " = " + colors.get(item) + "\n\t\t";
			
			// check colors - bugfix 'hack ???'
			if(item.endsWith("b")){
				output += item.substring(0, item.length()-1) + " = " + colors.get(item) + "\n\t\t";
			}
		}
		
		output += "\n</colors>\n<links>\n\t<link>\n\t\t";
		
		for(String item : linkparameter.keySet()){ // write all link parameter
			output += item + " = " + linkparameter.get(item) + "\n\t\t";
		}
		
		output += "<rules>\n\t\t<rule>\n\t\timportance = 100\n\t\tcondition  = 1\n\t\tcolor = eval(var(chr1))" +
				"\n\t\tflow = continue\n\t\t</rule>\n\t\t</rules>";
	
		output += "\n\t</link>\n</links>\n";
		
		output += "<plots>\n\t<plot>\n\t\t";
		for(String item : snaplotparameter.keySet()){ // write all sna parameter
			output += item + " = " + snaplotparameter.get(item) + "\n\t\t";
		}	
		
		output += "<backgrounds>\n\t\t\t<background>\n\t\t\t";
		for(String item : snabackgroundtparameter.keySet()){ // write all sna background parameter
			output += item + " = " + snabackgroundtparameter.get(item) + "\n\t\t\t";
		}	
		output += "</background>\n\t\t</backgrounds>\n\t\t";
		
		output += "<axes>\n\t\t";
		for(String item : snaaxesparameter.keySet()){ // write all sna axes parameter
			output += item + " = " + snaaxesparameter.get(item) + "\n\t\t";
		}	
		
		output += "<axis>\n\t\t\t spacing = 0.1r \n\t\t</axis>\n\t\t</axes>";
		
		output += "\n\t</plot>";
		
		// the second 2d ring
		output += "\n\t<plot>\n\t\t";
		for(String item : gpyparameter.keySet()){ // write all sna parameter
			output += item + " = " + gpyparameter.get(item) + "\n\t\t";
		}	
		
		output += "<backgrounds>\n\t\t\t<background>\n\t\t\t";
		for(String item : snabackgroundtparameter.keySet()){ // write all sna background parameter
			output += item + " = " + snabackgroundtparameter.get(item) + "\n\t\t\t";
		}	
		output += "</background>\n\t\t</backgrounds>\n\t\t";
		
		output += "<axes>\n\t\t";
		for(String item : snaaxesparameter.keySet()){ // write all sna axes parameter
			output += item + " = " + snaaxesparameter.get(item) + "\n\t\t";
		}	
		
		String stepsize = ""+Tools.roundTwoD(1.0d/(double)(nodes.maxGrowths()));
		output += "<axis>\n\t\t\t spacing = "+stepsize+"r \n\t\t</axis>\n\t\t</axes>";
		
		output += "\n\t</plot>" + "\n</plots>\n";
		
		if(useIdeogram){
			output += "<<include "+Settings.CIRCOS_BIN_PREFIX+"data/ideogram.conf>>\n";
		}
		
		if(useTicks){
			output += "<<include "+Settings.CIRCOS_BIN_PREFIX+"data/ticks.conf>>\n";
		}
		
		output += "<image>\n\t";
		for(String item : imageparameter.keySet()){ // write all image parameter
			output += item + " = " + imageparameter.get(item) + "\n\t";
		}			
		output += "\n</image>\n" +
				"\n<<include "+Settings.CIRCOS_BIN_PREFIX+"etc/colors_fonts_patterns.conf>>\n" +
						"<<include "+Settings.CIRCOS_BIN_PREFIX+"etc/housekeeping.conf>>\n" +
								"<<include "+Settings.CIRCOS_BIN_PREFIX+"etc/colors.conf>>";
		
		Tools.createFile(Settings.CIRCOS_DATA_PREFIX+"conf"+hashname+".txt", output);
	}

	/**
	 * This method will be used to write the configuration file for the growth per year circle of
	 * the visualization 
	 *  
	 * @param gpyNodes all nodes of the diagram
	 * @param hashname will be used to build the filename
	 */
	private void writeGrowthsPerYearHistogram(CircosNodeList gpyNodes,
			String hashname) {
		String outpath = Settings.CIRCOS_DATA_PREFIX+"gpy"+hashname+".txt"; // filename
		addParameter("gpy", "file", outpath);
		
		String gpyOut = "";
		for(CircosNode node : gpyNodes.getNodes()){
			TreeSet<Integer> years = new TreeSet<Integer>();
			for(String key : node.getGrowthsPerYear().keySet()){
				years.add(Integer.parseInt(key));
			}
			
			int count = node.getGrowthsPerYear().keySet().size();
			if(count == 0) count = 1;
			double step = Math.ceil(node.getSzMetricValue()) / count;
			
			for(int i = 1; i <= count; i++){
				long fromStep 		= Math.round(Math.ceil((i-1) * step));
				long toStep 		= Math.round(Math.ceil((i) * step));
				long forcedToStep 	= Math.round(Math.ceil((i) * step))+1;
				
				if(fromStep != toStep){
					gpyOut += node.getID() + Settings.CIRCOS_DELIMITER + fromStep  + Settings.CIRCOS_DELIMITER + 
						toStep + Settings.CIRCOS_DELIMITER + node.getGrowthsPerYear().get((""+years.pollFirst())) + "\n";
				}else{ // force at least one step in the visuzalization
					gpyOut += node.getID() + Settings.CIRCOS_DELIMITER + fromStep  + Settings.CIRCOS_DELIMITER + 
							forcedToStep + Settings.CIRCOS_DELIMITER + node.getGrowthsPerYear().get((""+years.pollFirst())) + "\n";
				}
			}
		}
		Tools.createFile(outpath,gpyOut);
	}

	/**
	 * This method will be used to write the configuration file for the SNA circle of
	 * the visualization 
	 * 
	 * @param snaNodes all nodes of the visualization
	 * @param hashname will be used to build the filename
	 */
	private void writeSnaHistogram(CircosNodeList snaNodes, String hashname) {
		String outpath = Settings.CIRCOS_DATA_PREFIX+"sna"+hashname+".txt";
		addParameter("sna", "file", outpath);
		double maxSNA = 0.0d;
		
		String snaOut = "";
		for(CircosNode node : snaNodes.getNodes()){
			snaOut += node.getID() + Settings.CIRCOS_DELIMITER + "0" + Settings.CIRCOS_DELIMITER + Math.round(node.getSzMetricValue()) + 
					Settings.CIRCOS_DELIMITER + Math.round(node.getSnaMetricValue()) + "\n";
			
			if(node.getSnaMetricValue() > maxSNA)
				maxSNA = node.getSnaMetricValue();
		}
		
		addParameter("sna", "max", ""+Math.round(maxSNA));
		Tools.createFile(outpath,snaOut);
	}	
}
