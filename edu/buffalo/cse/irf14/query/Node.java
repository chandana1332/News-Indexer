package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.List;

public class Node {

	public String name;
	public List<Integer> postingsList;

	public Node leftChild;
	public Node rightChild;

	public Node(String name) {
		this.name = name;
		this.postingsList = new ArrayList<Integer>();
	}

}
