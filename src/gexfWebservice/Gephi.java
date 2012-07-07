package gexfWebservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
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

public class Gephi{
	/**
	 * This methods rounds a given double value 
	 * @param value
	 * @return rounded double value
	 */
	private double roundTwoD(double value) {
		double result = value * 100;
		result = Math.round(result);
		result = result / 100;
		return result;
	}
	
	CircosTuple fillCircos(String path, String metric, CircosTuple fillThis){
		CircosNodeList mynodes = new CircosNodeList();
		CircosEdgeList myedges = new CircosEdgeList();
		
		//Init a project
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file       
        Container container;
        try {
            File file = new File(path);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

		importController.process(container, new DefaultProcessor(), workspace);
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
		
		for(Node node : graph.getNodes()){
			Double centrality = (Double)node.getNodeData().getAttributes().getValue(metricCol.getIndex());
			mynodes.addNode(node.getNodeData().getId(), node.getNodeData().getLabel(), centrality * 100);
		}
		
		for(Edge edge : graph.getEdges()){
			myedges.addEdge(edge.getEdgeData().getSource().getId(), edge.getEdgeData().getTarget().getId(), 2);
		}
		
		fillThis.setEdges(myedges);
		fillThis.setNodes(mynodes);
		
		return fillThis;
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
        Container container;
        try {
            File file = new File(path);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }

		importController.process(container, new DefaultProcessor(), workspace);
		DirectedGraph graph = graphModel.getDirectedGraph();
		
		return graph.getNodeCount();
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

        //Import file       
        Container container;
        try {
            File file = new File(path);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }

		importController.process(container, new DefaultProcessor(), workspace);
		DirectedGraph graph = graphModel.getDirectedGraph();
		
		return graph.getEdgeCount();
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

        //Import file       
        Container container;
        try {
            File file = new File(path);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
		
		//Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);

		GraphDensity dens = new GraphDensity();
		dens.setDirected(false);
		dens.execute(graphModel, attributeModel);	
		
		return dens.getDensity();
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
        Container container;
        try {
        	System.out.println("opening: "+filename);
            File file = new File(filename);
        	container = importController.importFile(file);
            container.getLoader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ERROR";
        }
		
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
				standardizedCloseness = roundTwoD(1 / centrality);
			
		   cc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedCloseness, centrality));
		}
		
		//Iterate over values
		for (Node n : graph.getNodes()) {
		   Double centrality = (Double)n.getNodeData().getAttributes().getValue(betweennessColumn.getIndex());
		   
		   double standardizedBetweenness = roundTwoD(centrality / (((cnodes - 1) * (cnodes - 2))/2));
			
		   bc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedBetweenness, centrality));
		}
		
		//Iterate over values
		for (Node n : graph.getNodes()) {
		   int centrality = (Integer)n.getNodeData().getAttributes().getValue(degreeColumn.getIndex());
		   
		   double standardizedDegree = roundTwoD((double) ((double) centrality / (double) (cnodes -1)));
		   
		   dc.add(new MyNode(n.getNodeData().getId(),n.getId(),standardizedDegree, centrality));
		}
		
		Collections.sort(cc,new NodeComparator());
		Collections.sort(bc,new NodeComparator());
		Collections.sort(dc,new NodeComparator());
		
		String output = "<top>\n\t<closenessCentrality>\n";
		for(int i=0; i < cc.size(); i++){
			String clearlabel = graph.getNode(cc.get(i).getSystemID()).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			output += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + cc.get(i).getId() + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<svalue>"+ cc.get(i).getStandardizedValue() + "</svalue>\n" +
					"\t\t\t<value>"+ cc.get(i).getValue() + "</value>\n" +
							"\t\t</ranks>\n";
		}
		output +="\t</closenessCentrality>\n";
		
		output += "\t<betweennessCentrality>\n";
		for(int i=0; i < bc.size(); i++){
			String clearlabel = graph.getNode(bc.get(i).getSystemID()).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			output += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + bc.get(i).getId() + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<svalue>"+ bc.get(i).getStandardizedValue() + "</svalue>\n" +
					"\t\t\t<value>"+ bc.get(i).getValue() + "</value>\n" +
							"\t\t</ranks>\n";
		}
		output +="\t</betweennessCentrality>\n";
		
		output += "\t<degreeCentrality>\n";
		for(int i=0; i < dc.size(); i++){
			String clearlabel = graph.getNode(dc.get(i).getSystemID()).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			output += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + dc.get(i).getId() + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<svalue>"+ dc.get(i).getStandardizedValue() + "</svalue>\n" +
					"\t\t\t<value>"+ dc.get(i).getValue() + "</value>\n" +
							"\t\t</ranks>\n";
		}
		output +="\t</degreeCentrality>\n</top>";
		
		return output;
	}
	
	Gephi(){	
		

	}
}
