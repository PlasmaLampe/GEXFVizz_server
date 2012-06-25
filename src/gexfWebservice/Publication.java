package gexfWebservice;

import java.util.ArrayList;
import java.util.HashMap;

public class Publication{
	private String id;
	private ArrayList<String> cites;
	private HashMap<String, Integer> citedTogetherWith;
	private String title;
	
	/**
	 * @param id
	 * @param cites
	 */
	public Publication(String id, String title, ArrayList<String> cites) {
		super();
		this.id = id;
		this.cites = cites;
		this.title = title;
		this.citedTogetherWith = new HashMap<String, Integer>();
	}
	
	public void addPaperThatHasBeenCitedWithThisOne(String paper){
		if(citedTogetherWith.containsKey(paper)){
			int count = citedTogetherWith.get(paper);
			citedTogetherWith.remove(paper);
			citedTogetherWith.put(paper, count+1);
		}else{
			citedTogetherWith.put(paper, 1);
		}
	}
	
	
	/**
	 * @return the citedTogetherWith
	 */
	public HashMap<String, Integer> getCitedTogetherWith() {
		return citedTogetherWith;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the cites
	 */
	public ArrayList<String> getCites() {
		return cites;
	}
	/**
	 * @param cites the cites to set
	 */
	public void setCites(ArrayList<String> cites) {
		this.cites = cites;
	}
}
