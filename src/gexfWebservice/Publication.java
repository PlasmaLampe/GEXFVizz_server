package gexfWebservice;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Each instance of this class represents a publication. This class also stores additional information that
 * is needed to build the "co-citation" and "bibliographic coupling" graphs 
 * 
 * @author Jörg Amelunxen
 *
 */
public class Publication{
	private String id;
	private ArrayList<String> cites; // publications that are cited by this one
	private HashMap<String, Integer> citedTogetherWith; // needed for Co-Citation
	private HashMap<String, Integer> bibliograpiccoupling;
	private String title;
	private ArrayList<String> getCitedBy; // publication that cite this one
	private int publishedInYear;

	
	public Publication(String id, int publishedInYear, String title, ArrayList<String> cites, ArrayList<String> getCitedBy) {
		super();
		this.id = id;
		this.cites = cites;
		this.title = title;
		this.citedTogetherWith = new HashMap<String, Integer>();
		this.bibliograpiccoupling = new HashMap<String, Integer>();
		this.getCitedBy = getCitedBy;
		this.publishedInYear = publishedInYear;
		
	}
	
	/**
	 * This method adds a paper that has been cited together with this paper
	 * 
	 * @param paper the id of the paper
	 */
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
	 * @return the year in which the publication has been published
	 */
	public int getPublishedInYear() {
		return publishedInYear;
	}

	/**
	 * This method adds a bibliographic coupling value to this paper
	 * 
	 * @param paper the id of the target paper
	 * @param value the bibliographic coupling value
	 */
	public void addBibliographicCouplingTo(String paper, int value){
		bibliograpiccoupling.put(paper, value);
	}
	
	
	/**
	 * @return the bibliographic coupling
	 */
	public HashMap<String, Integer> getBibliograpiccoupling() {
		return bibliograpiccoupling;
	}

	/**
	 * @return the getCitedBy
	 */
	public ArrayList<String> getGetCitedBy() {
		return getCitedBy;
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
