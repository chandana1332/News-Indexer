package edu.buffalo.cse.irf14.analysis;

import java.io.Serializable;

public class AuthorData implements Serializable {

	private static final long serialVersionUID = 1L;
	private int authorId;
	private String authorOrg;

	public AuthorData() {
		// TODO Auto-generated constructor stub
	}

	public String toString() {
		return this.authorId + this.authorOrg;
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorName) {
		this.authorId = authorName;
	}

	public String getAuthorOrg() {
		return authorOrg;
	}

	public void setAuthorOrg(String authorOrg) {
		this.authorOrg = authorOrg;
	}

}
