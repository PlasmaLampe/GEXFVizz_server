/**
 * 
 */
package gexfWebservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * This class stores some methods, which are used to simplify the code 
 * of the main application
 * 
 * @author Jörg Amelunxen
 *
 */
public class Tools {
    /**
     * The methods checks if a given file exists. 
     * If the file does not exist, the method creates it and
     * inserts the content of the given file
     *  
     * @param path the file that has to be checked
     * @param content if the file has to be created, use
     * this string as content for the file
     */
	public static void doesFileExist(String path, String stringcontent){
    	File f = new File(path);
    	if(f.exists()) {
    		/* do nothing, the file has already been hashed and saved */ 
    	}else{
    		Tools.createFile(path,stringcontent);
    	}
    }
    
    /**
     * The method creates the specified file with the given content
     * @param path to the file, that is going to be created
     * @param content the content of the file
     */
	public static void createFile(String path, String content){	
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
     * This method reads the content of a given file
     * 
     * @param path the path to the file
     * @return the content of the file as a String
     */
	public static String getContent(String path){
    	File tempfile = new File(path);
    	String contentOfFile ="";

    	try {
    		BufferedReader input =  new BufferedReader(new FileReader(tempfile));
    		try {
    			String line = null; 
    			while (( line = input.readLine()) != null){
    				contentOfFile += line;
    			}
    		}finally {
    			input.close();
    		}
    	}catch (Exception e){
    		e.printStackTrace();
    	}

    	return contentOfFile.toString();
    }
    
	/**
	 * This methods rounds a given double value 
	 * @param value
	 * @return rounded double value
	 */
	public static double roundTwoD(double value) {
		double result = value * 100;
		result = Math.round(result);
		result = result / 100;
		return result;
	}
	
}
