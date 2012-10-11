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
	private static final long MEGABYTE = 1024L * 1024L;
	
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

	/**
	 * Converts bytes to megabytes
	 * @param bytes that should be converted
	 * @return the given bytes as megabytes
	 */
	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}
	
	/**
	 * This method initializes the global methods of the
	 * application with the help of the settings.txt file
	 */
	public static void initParameter(){
    	File tempfile = new File(Settings.CFG_FILE);

    	try {
    		BufferedReader input =  new BufferedReader(new FileReader(tempfile));
    		try {
    			String line = null; 
    			while (( line = input.readLine()) != null){
    				String token[] =  line.split("=");
    				
    				switch(token[0].replaceAll("\t", "").trim()){
    				case "DB_USER":
    					Settings.DB_USER = token[1];
    					break;
    				case "DB_PASSWORD":
    					Settings.DB_PASSWORD = token[1];
    					break;		
    				case "PATH_TO_DATABASES":
    					Settings.PATH_TO_DATABASES = token[1];
    					break;
    				case "MYSQL_PREFIX":
    					Settings.MYSQL_PREFIX = token[1];
    					break;
    				case "APACHE_PATH":
    					Settings.APACHE_PATH = token[1];
    					break;
    				case "CIRCOS_BIN_PREFIX":
    					Settings.CIRCOS_BIN_PREFIX = token[1];
    					break;
    				case "CIRCOS_GFX_PREFIX":
    					Settings.CIRCOS_GFX_PREFIX = token[1];
    					break;
    				case "CIRCOS_DATA_PREFIX":
    					Settings.CIRCOS_DATA_PREFIX = token[1];
    					break;
    				case "CIRCOS_DATA_ORDERCHR":
    					Settings.CIRCOS_DATA_ORDERCHR = token[1];
    					break;
    				case "DOMAIN_PREFIX":
    					Settings.DOMAIN_PREFIX = token[1];
    					break;
    				case "HTTPIP":
    					Settings.HTTPIP = token[1];
    					break;
    				case "TomcatURLToServlet":
    					Settings.TomcatURLToServlet = token[1];
    					break;
    				case "DEBUG":
    					Settings.DEBUG = Boolean.parseBoolean(token[1]);
    					break;
    				case "TESTPATH":
    					Settings.TESTPATH = token[1];
    					break;
    				case "MEMORY_FILE":
    					Settings.MEMORY_FILE = token[1];
    					break;
    				}
    			}
    		}finally {
    			input.close();
    		}
    	}catch (Exception e){
    		e.printStackTrace();
    	}
	}
}
