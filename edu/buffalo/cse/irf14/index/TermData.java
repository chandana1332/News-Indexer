package edu.buffalo.cse.irf14.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class TermData implements Serializable{
	
	
	private List<Integer> postingsList;
	private Map<Integer, Integer> termFrequency;
	private int documentLength;
	private double idf;

	public TermData() {
		// TODO Auto-generated constructor stub
		this.postingsList = new ArrayList<Integer>();
		this.termFrequency = new HashMap<Integer, Integer>();
		this.documentLength = 0;
		this.idf = 0.0f;
	}

	public Map<Integer, Integer> getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Map<Integer, Integer> termFrequency) {
		this.termFrequency = termFrequency;
	}
	
	public String toString(){
		return termFrequency.toString();
	}

	public int getDocumentLength() {
		return documentLength;
	}

	public void setDocumentLength(int documentLength) {
		this.documentLength = documentLength;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public List<Integer> getPostingsList() {
		return postingsList;
	}

	public void setPostingsList(List<Integer> postingsList) {
		this.postingsList = postingsList;
	}

}
