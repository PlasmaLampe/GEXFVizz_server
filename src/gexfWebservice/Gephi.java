package gexfWebservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

class MyNode{
	int id;			// 'real id' of the graph file
	int systemID;	// this id is gephi specific and is needed for computations
	double value;
	
	MyNode(int id, int systemID, double value){
		this.id = id;
		this.systemID = systemID;
		this.value = value;
	}
}

class NodeComparator implements Comparator<MyNode> {
    @Override
    public int compare(MyNode o1, MyNode o2) {
        return (int)((o2.value * 100) - (o1.value * 100));
    }
}

public class Gephi{
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

		DirectedGraph graph = graphModel.getDirectedGraph();
		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel, attributeModel);
		 
		AttributeColumn betweennessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
		
		ArrayList<MyNode> cc = new ArrayList<MyNode>();
		ArrayList<MyNode> bc = new ArrayList<MyNode>();
		
		//Iterate over values
		for (Node n : graph.getNodes()) {
		   Double centrality = (Double)n.getNodeData().getAttributes().getValue(centralityColumn.getIndex());
		   cc.add(new MyNode(Integer.parseInt(n.getNodeData().getId()),n.getId(),centrality));
		}
		
		//Iterate over values
		for (Node n : graph.getNodes()) {
		   Double centrality = (Double)n.getNodeData().getAttributes().getValue(betweennessColumn.getIndex());
		   bc.add(new MyNode(Integer.parseInt(n.getNodeData().getId()),n.getId(),centrality));
		}
		
		Collections.sort(cc,new NodeComparator());
		Collections.sort(bc,new NodeComparator());
		
		String output = "<top>\n\t<closenessCentrality>\n";
		for(int i=0; i < cc.size(); i++){
			String clearlabel = graph.getNode(cc.get(i).systemID).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			output += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + cc.get(i).id + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<value>"+ cc.get(i).value + "</value>\n" +
							"\t\t</ranks>\n";
		}
		output +="\t</closenessCentrality>\n";
		
		output += "\t<betweennessCentrality>\n";
		for(int i=0; i < bc.size(); i++){
			String clearlabel = graph.getNode(bc.get(i).systemID).getNodeData().getLabel();
			String clearedlabel = clearlabel.replaceAll("[']|[<]|[>]", "");
			
			output += "\t\t<ranks>\n " +
					"\t\t\t<place>"+ (i+1) +"</place>\n " +
					"\t\t\t<id>" + bc.get(i).id + "</id>\n" +
					"\t\t\t<label>"+ clearedlabel +"</label>\n" + 
					"\t\t\t<value>"+ bc.get(i).value + "</value>\n" +
							"\t\t</ranks>\n";
		}
		output+="\t</betweennessCentrality>\n</top>";
		
		//System.out.println(output);
		return output;
	}
	
	Gephi(){	
		

	}
}
