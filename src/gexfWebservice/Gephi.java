package gexfWebservice;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class handles the communication the the Gephi Toolkit
 * 
 * @author Jörg Amelunxen
 *
 */
public class Gephi{
    /**
     * This method creates a CircosTuple with all edges and nodes of the gexf graph
     * @param path to the gexf graph
     * @param metric the selected SNA metric
     * @param rank maximal amount of top entries
     * @return the filled CircosTuple
     */
	CircosTuple fillCircos(String path, String metric, int rank){
		CircosNodeList mynodes = new CircosNodeList();
		CircosEdgeList myedges = new CircosEdgeList();
		
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		 
		Container container = importFileToContainer(path, importController);
		 
		if(container != null){
			importController.process(container, new DefaultProcessor(), workspace);
	    	GephiYearExtractor gex = extractDatesFromGEXF(path);
	    	
			UndirectedGraph graph = graphModel.getUndirectedGraph();
			GraphDistance distance = new GraphDistance();
			distance.setDirected(false);
			distance.execute(graphModel, attributeModel);

			Degree deg = new Degree();
			deg.execute(graphModel, attributeModel);

			AttributeColumn metricCol = null;
			switch(metric){
			case "cc":
				metricCol = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
				break;
			case "dc":
				metricCol = attributeModel.getNodeTable().getColumn(Degree.DEGREE);
				break;
			case "bc":
				metricCol = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);;
				break;
			}
			
			HashMap <String, Double> adjacent_nodes_sum = new HashMap<String,Double>();
			for(Node node : graph.getNodes()){
				HashMap<String,Integer> growthsPerYear = new HashMap<String,Integer>();
				
				// we need the sum of the weights of the adjacent edges
				EdgeIterable adjacentEdges = graph.getEdges(node);
				
				double sum = 0.0d;
				for(Edge adjacentedge : adjacentEdges){
					sum += adjacentedge.getWeight();
					
					Node othernode = getOtherNodeOnAdjacentEdge(node, adjacentedge);
					String spawn = gex.getStartOfID(othernode.getNodeData().getId());
					
					if(growthsPerYear.containsKey(spawn)){ 		// for every node that spawns in this year
						int temp = growthsPerYear.get(spawn); 	// increase the growthsPerYear 
						growthsPerYear.remove(spawn);
						growthsPerYear.put(spawn, temp+1);
					}else{
						growthsPerYear.put(spawn, 1);
					}
				}
				
				// add missing years as "zero entries"
				HashSet<Integer> missingYears = gex.getSetOfMissingDates(growthsPerYear.keySet());
				for(int myear : missingYears){
					growthsPerYear.put(""+myear, 0);
				}
				
				// create the node
				String stringcentrality = "" + node.getNodeData().getAttributes().getValue(metricCol.getIndex());
				Double centrality = Double.parseDouble(stringcentrality);
				mynodes.addNode(node.getNodeData().getId(), node.getNodeData().getLabel(), centrality, sum, growthsPerYear);
				
				// store temp data
				adjacent_nodes_sum.put(node.getNodeData().getId(), 0.0d);
			}

			for(Edge edge : graph.getEdges()){
				double offsetS = adjacent_nodes_sum.get(edge.getEdgeData().getSource().getId());
				double offsetE = adjacent_nodes_sum.get(edge.getEdgeData().getTarget().getId());
				
				myedges.addEdge(edge.getEdgeData().getSource().getId(), edge.getEdgeData().getTarget().getId(), edge.getWeight(), 
						offsetS,offsetE);
				
				adjacent_nodes_sum.remove(edge.getEdgeData().getSource());
				adjacent_nodes_sum.remove(edge.getEdgeData().getTarget());
				
				adjacent_nodes_sum.put(edge.getEdgeData().getSource().getId(), offsetS + edge.getWeight());
				adjacent_nodes_sum.put(edge.getEdgeData().getTarget().getId(), offsetE + edge.getWeight());
			}

			mynodes.cutAfterRank(rank);
			myedges.cleanEdgeList(mynodes);

			CircosTuple tuple = new CircosTuple(myedges, mynodes);

			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();

			return tuple;
		}
		else 
			return null;
	}

	/**
	 * This method extracts the start and end years of a GEXF file, because this
	 * feature is buggy within the Gephi Toolkit 
	 * @param path to the gexf file
	 * @return a GephiYearExtractor, which contains the start and end years of all nodes
	 */
	private GephiYearExtractor extractDatesFromGEXF(String path) {
		XMLReader xmlReader = null;
		GephiYearExtractor gex = new GephiYearExtractor();
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		String xml = Tools.getContent(path);
		InputSource inputSource = new InputSource(new StringReader(xml));
		xmlReader.setContentHandler(gex);
		try {
			xmlReader.parse(inputSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return gex;
	}

	/**
	 * Returns the node, that is on the 'other side' of the edge
	 * @param node the node, that should not be returned
	 * @param adjacentedge the edge
	 * @return the node, that is on the other side
	 */
	private Node getOtherNodeOnAdjacentEdge(Node node, Edge adjacentedge) {
		if(adjacentedge.getTarget().getNodeData().getId().equals(node.getNodeData().getId())){
			return adjacentedge.getSource();
		}else if(adjacentedge.getSource().getNodeData().getId().equals(node.getNodeData().getId())){
			return adjacentedge.getTarget();
		}
		return null;
	}
	/**
	 * returns the #nodes of the given file
	 * @param path The file which should be checked
	 * @return #nodes as integer value
	 */
	int getNodes(String path){
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file       
        Container container = importFileToContainer(path, importController);
        if(container != null){
			importController.process(container, new DefaultProcessor(), workspace);
			DirectedGraph graph = graphModel.getDirectedGraph();
			
			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();
			
			return graph.getNodeCount();
        }else{
        	return -1;
        }
	}

	/**
	 * This method is used for loading a GEXF file
	 * @param path to the file that should be loaded
	 * @param importController a ImpotController of the Gephi Toolkit
	 * @return the container that contains the graph of the given GEXF file
	 */
	private Container importFileToContainer(String path, ImportController importController) {
		Container container;
        try {
            File file = new File(path);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
		return container;
	}
	
	/**
	 * returns the #edges of the given file
	 * @param path The file which should be checked
	 * @return #edges as integer value
	 */
	int getEdges(String path){
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        Container container = importFileToContainer(path, importController);
        if(container != null){
			importController.process(container, new DefaultProcessor(), workspace);
			DirectedGraph graph = graphModel.getDirectedGraph();
			
			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();
			
			return graph.getEdgeCount();
        }else{
        	return -1;
        }
	}
	
	/**
	 * returns the graph density of the given file
	 * @param path The file which should be checked
	 * @return density as double value
	 */
	double getDensity(String path){
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        Container container = importFileToContainer(path, importController);
		if(container != null){
			//Append imported data to GraphAPI
			importController.process(container, new DefaultProcessor(), workspace);
			
			GraphDensity dens = new GraphDensity();
			dens.setDirected(false);
			dens.execute(graphModel, attributeModel);	
			
			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();
			
			return dens.getDensity();
        }else{
        	return -1.0;
        }	
	}
	
	/**
	 * returns a XML string which contains all available metrics
	 * about the nodes on this graph
	 * @param filename 
	 * @return a XML string
	 */
	String getMetrics(String filename){
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file       
		Container container = importFileToContainer(filename, importController);
		if(container != null){
			//Append imported data to GraphAPI
			importController.process(container, new DefaultProcessor(), workspace);
	
			UndirectedGraph graph = graphModel.getUndirectedGraph();
			GraphDistance distance = new GraphDistance();
			distance.setDirected(false);
			distance.execute(graphModel, attributeModel);
			
			Degree deg = new Degree();
			deg.execute(graphModel, attributeModel);
			 
			AttributeColumn betweennessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
			AttributeColumn closenessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
			AttributeColumn degreeColumn = attributeModel.getNodeTable().getColumn(Degree.DEGREE);
			
			ArrayList<MyNode> cc = new ArrayList<MyNode>();
			ArrayList<MyNode> bc = new ArrayList<MyNode>();
			ArrayList<MyNode> dc = new ArrayList<MyNode>();
			
			int cnodes = graph.getNodeCount();
			
			//Iterate over values
			for (Node n : graph.getNodes()) {
			   Double centrality = (Double)n.getNodeData().getAttributes().getValue(closenessColumn.getIndex());
			   
			   double standardizedCloseness = 0;
				// use the reciprocal of the value calculated by Gephi
				if(centrality != 0.0)
					standardizedCloseness = Tools.roundTwoD(1 / centrality);
				
			   cc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedCloseness, centrality));
			}
			
			//Iterate over values
			for (Node n : graph.getNodes()) {
			   Double centrality = (Double)n.getNodeData().getAttributes().getValue(betweennessColumn.getIndex());
			   
			   double standardizedBetweenness = Tools.roundTwoD(centrality / (((cnodes - 1) * (cnodes - 2))/2));
				
			   bc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedBetweenness, centrality));
			}
			
			//Iterate over values
			for (Node n : graph.getNodes()) {
			   int centrality = (Integer)n.getNodeData().getAttributes().getValue(degreeColumn.getIndex());
			   
			   double standardizedDegree = Tools.roundTwoD((double) ((double) centrality / (double) (cnodes -1)));
			   
			   dc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedDegree, centrality));
			}
			
			Collections.sort(cc,new NodeComparator());
			Collections.sort(bc,new NodeComparator());
			Collections.sort(dc,new NodeComparator());
			
			String output = "<top>\n\t<filename>"+filename+"</filename>\n\t<closenessCentrality>\n";
			output += printToXML(graph, cc);
			output +="\t</closenessCentrality>\n";
			
			output += "\t<betweennessCentrality>\n";
			output += printToXML(graph, bc);
			output +="\t</betweennessCentrality>\n";
			
			output += "\t<degreeCentrality>\n";
			output += printToXML(graph, dc);
			output +="\t</degreeCentrality>\n</top>";
			
			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();
			
			return output;
        }else{
        	return "ERROR";
        }
	}

	/**
	 * This method prints the nodes of the graph to an XML String with additional information
	 * 
	 * @param graph that should be printed
	 * @param dc contains an ArrayList of MyNodes that are needed to get the additional information (for example szientometric values)
	 * @return an XML String
	 */
	private String printToXML(UndirectedGraph graph, ArrayList<MyNode> dc) {
		String tempout = "";
		for(int i=0; i < dc.size(); i++){
			String clearlabel = graph.getNode(dc.get(i).getSystemID()).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			tempout += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + dc.get(i).getId() + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<svalue>"+ dc.get(i).getStandardizedValue() + "</svalue>\n" +
					"\t\t\t<value>"+ dc.get(i).getValue() + "</value>\n" +
							"\t\t</ranks>\n";
		}
		return tempout;
	}
	
	Gephi(){	
		

	}

	/**
	 * This method stores the gephi project file at the path specified in the Settings file
	 * 
	 * @param hashPath is the hashname of the file and is used to build the filename
	 * @return the URL to the project file 
	 */
	public String getProject(String hashPath) {
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        Container container = importFileToContainer(hashPath, importController);
		if(container != null){
			importController.process(container, new DefaultProcessor(), workspace);
			String name = hashPath.substring(hashPath.lastIndexOf("/")+1, hashPath.lastIndexOf("."));
			String projectpath = Settings.APACHE_PATH + "data/" + name + ".gephi";
			Runnable run = pc.saveProject(pc.getCurrentProject(), new File(projectpath));
			run.run();
			pc.closeCurrentWorkspace();
			pc.closeCurrentProject();
			
			return Settings.HTTPIP + "data/" + name + ".gephi";
        }else{
        	return "ERROR";
        }
	}

}
