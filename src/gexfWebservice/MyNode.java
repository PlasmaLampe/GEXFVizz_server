package gexfWebservice;

public class MyNode{
	String id;			// 'real id' of the graph file
	int systemID;	// this id is gephi specific and is needed for computations
	double value;
	
	MyNode(String string, int systemID, double value){
		this.id = string;
		this.systemID = systemID;
		this.value = value;
	}
}

