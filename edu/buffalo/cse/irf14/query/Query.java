package edu.buffalo.cse.irf14.query;

import java.util.Map;

/**
 * Class that represents a parsed query
 * 
 * @author chandana
 *
 */
public class Query {
	/**
	 * Method to convert given parsed query into string
	 */
	private String querystring;
	private Map<String, Integer> queryTerms;

	public String getQuerystring() {
		return querystring;
	}

	public void setQuerystring(String querystring) {
		this.querystring = querystring;
	}

	public String toString() {
		// TODO: YOU MUST IMPLEMENT THIS
		return this.querystring;

	}

	public Map<String, Integer> getQueryTerms() {
		return queryTerms;
	}

	public void setQueryTerms(Map<String, Integer> queryTerms) {
		this.queryTerms = queryTerms;
	}

}
