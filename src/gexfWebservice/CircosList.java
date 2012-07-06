package gexfWebservice;

import java.io.BufferedWriter;
import java.io.FileWriter;

public abstract class CircosList {
	protected String output = "";
	
    /**
     * The method creates the specified file with the given content
     * @param path to the file, that is going to be created
     * @param content the content of the file
     */
    protected void createFile(String path, String content){
		try{
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
    }
    
    protected String createID(String label){
    	return label.replace(' ', '_');
    }
    
	public void writeFile(String hashname){}
}
