package edu.buffalo.cse.irf14.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.index.TermData;

public class BinaryTree {

	public Node root;
	private String indexDir;
	private Map<String, Integer> termDictionary;
	private Map<Integer, TermData> termIndex;
	private Map<Integer, String> documentDictionary;

	public Node addNode(Node left, Node r, Node right) {

		// Create a new Node and initialize it

		// If there is no root this becomes root

		if (root == null) {
			root = r;
			root.leftChild = left;
			root.rightChild = right;
			return root;

		} else {
			r.leftChild = left;
			r.rightChild = right;
			return r;
		}

	}

	// All nodes are visited in ascending order
	// Recursion is used to go to one node and
	// then go to its child nodes and so forth

	public void inOrderTraverseTree(Node focusNode) {

		if (focusNode != null) {

			// Traverse the left node

			inOrderTraverseTree(focusNode.leftChild);

			// Visit the currently focused on node

			// System.out.println(focusNode.name);
			String term = focusNode.name;

			if (term.equalsIgnoreCase("or") || term.equalsIgnoreCase("and")) {
				Node leftChild = focusNode.leftChild;
				Node rightChild = focusNode.rightChild;

				if (leftChild.postingsList.size() == 0) {
					String leftTerm = leftChild.name;
					boolean not = leftTerm.contains("<");

					if (leftTerm.toLowerCase().contains("category")) {
						leftChild.postingsList = categoryLookUp(leftTerm, not);
					} else if (leftTerm.toLowerCase().contains("place")) {
						leftChild.postingsList = placeLookUp(leftTerm, not);
					} else if (leftTerm.toLowerCase().contains("author")) {
						leftChild.postingsList = authorLookUp(leftTerm, not);
					} else if (term.toLowerCase().contains("term")) {
						leftChild.postingsList = termLookUp(leftTerm, not);
					}
				}

				if (rightChild.postingsList.size() == 0) {
					String rightTerm = rightChild.name;
					boolean not = rightTerm.contains("<");

					if (rightTerm.toLowerCase().contains("category")) {
						rightChild.postingsList = categoryLookUp(rightTerm, not);
					} else if (rightTerm.toLowerCase().contains("place")) {
						rightChild.postingsList = placeLookUp(rightTerm, not);
					} else if (rightTerm.toLowerCase().contains("author")) {
						rightChild.postingsList = authorLookUp(rightTerm, not);
					} else if (rightTerm.toLowerCase().contains("term")) {
						rightChild.postingsList = termLookUp(rightTerm, not);
					}
				}

				if (term.equalsIgnoreCase("or")) {
					focusNode.postingsList = or(leftChild, rightChild);
				} else if (term.equalsIgnoreCase("and")) {
					focusNode.postingsList = and(leftChild, rightChild);
				}
			} else {
				boolean not = term.contains("<");

				if (term.toLowerCase().contains("category")) {
					focusNode.postingsList = categoryLookUp(term, not);
				} else if (term.toLowerCase().contains("place")) {
					focusNode.postingsList = placeLookUp(term, not);
				} else if (term.toLowerCase().contains("author")) {
					focusNode.postingsList = authorLookUp(term, not);
				} else if (term.toLowerCase().contains("term")) {
					focusNode.postingsList = termLookUp(term, not);
				}
			}

			// Traverse the right node

			inOrderTraverseTree(focusNode.rightChild);

		}

	}

	@SuppressWarnings("unchecked")
	public List<Integer> categoryLookUp(String term, boolean not) {
		try {
			File dir = new File(this.indexDir);
			ObjectInputStream ois;
			if (dir.exists()) {
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Category Dictionary.ser"));
				Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
						.readObject();

				dir = new File(this.indexDir + File.separator + "category");
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Category Index.ser"));
				Map<Integer, TermData> index = (TreeMap<Integer, TermData>) ois
						.readObject();
				ois.close();

				if (not) {
					term = term.substring(term.indexOf(':') + 1,
							term.indexOf('>'));
					term = term.replace("\"", "");
					Integer id = dictionary.get(term);
					List<Integer> postings = new ArrayList<Integer>();
					if (id != null) {
						TermData termData = index.get(id);
						if (termData != null)
							postings = termData.getPostingsList();
					}

					List<Integer> notPostings = new ArrayList<Integer>();

					for (Entry<Integer, String> entry : this.documentDictionary
							.entrySet()) {
						int docId = entry.getKey();
						if (!postings.contains(docId))
							notPostings.add(docId);
					}
					return notPostings;
				} else {
					term = term.substring(term.indexOf(':') + 1);
					term = term.replace("\"", "");
					Integer id = dictionary.get(term);
					if (id != null) {
						TermData termData = index.get(dictionary.get(term));
						if (termData != null)
							return index.get(dictionary.get(term))
									.getPostingsList();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Integer>();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> authorLookUp(String term, boolean not) {

		try {
			File dir = new File(this.indexDir);
			ObjectInputStream ois;
			if (dir.exists()) {
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Author Dictionary.ser"));
				Map<String, String> dictionary = (TreeMap<String, String>) ois
						.readObject();

				dir = new File(this.indexDir + File.separator + "author");
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Author Index.ser"));
				Map<Integer, List<Integer>> index = (TreeMap<Integer, List<Integer>>) ois
						.readObject();
				ois.close();

				// System.out.println(dictionary);
				// System.out.println(index);

				if (not) {
					term = term.substring(term.indexOf(':') + 1,
							term.indexOf('>'));
					term = term.replace("\"", "");
					// String id = dictionary.get(term);
					List<Integer> postings = new ArrayList<Integer>();
					// if(id != null)
					postings = index.get(term);
					List<Integer> notPostings = new ArrayList<Integer>();
					for (Entry<Integer, String> entry : this.documentDictionary
							.entrySet()) {
						int docId = entry.getKey();
						if (!postings.contains(docId))
							notPostings.add(docId);
					}
					return notPostings;
				} else {
					term = term.substring(term.indexOf(':') + 1);
					term = term.replace("\"", "");
					// String id = dictionary.get(term);
					if (index.get(term) != null)
						return index.get(term);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Integer>();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> placeLookUp(String term, boolean not) {

		try {
			File dir = new File(this.indexDir);
			ObjectInputStream ois;
			if (dir.exists()) {
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Place Dictionary.ser"));
				Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
						.readObject();

				dir = new File(this.indexDir + File.separator + "place");
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Place Index.ser"));
				Map<Integer, TermData> index = (TreeMap<Integer, TermData>) ois
						.readObject();
				ois.close();

				// System.out.println(dictionary);
				// System.out.println(index);

				if (not) {
					term = term.substring(term.indexOf(':') + 1,
							term.indexOf('>'));
					term = term.replace("\"", "");
					Integer id = dictionary.get(term);
					List<Integer> postings = new ArrayList<Integer>();
					if (id != null) {
						TermData termData = index.get(id);
						if (termData != null)
							postings = termData.getPostingsList();
					}
					List<Integer> notPostings = new ArrayList<Integer>();
					for (Entry<Integer, String> entry : this.documentDictionary
							.entrySet()) {
						int docId = entry.getKey();
						if (!postings.contains(docId))
							notPostings.add(docId);
					}
					return notPostings;
				} else {
					term = term.substring(term.indexOf(':') + 1);
					term = term.replace("\"", "");
					Integer id = dictionary.get(term);
					if (id != null) {
						TermData termData = index.get(id);
						if (termData != null)
							return termData.getPostingsList();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Integer>();
	}

	public List<Integer> termLookUp(String term, boolean not) {
		if (not) {
			term = term.substring(term.indexOf(':') + 1, term.indexOf('>'));
			term = term.replace("\"", "");
			Integer id = this.termDictionary.get(term);
			List<Integer> postings = new ArrayList<Integer>();
			if (id != null) {
				TermData termData = this.termIndex.get(id);
				if (termData != null)
					postings = termData.getPostingsList();
			}
			List<Integer> notPostings = new ArrayList<Integer>();
			for (Entry<Integer, String> entry : this.documentDictionary
					.entrySet()) {
				int docId = entry.getKey();
				if (!postings.contains(docId))
					notPostings.add(docId);
			}
			return notPostings;
		} else {
			term = term.substring(term.indexOf(':') + 1);
			term = term.replace("\"", "");
			Integer id = this.termDictionary.get(term);
			if (id != null) {
				TermData termData = this.termIndex.get(id);
				if (termData != null)
					return termData.getPostingsList();
			}
		}

		return new ArrayList<Integer>();
	}

	public List<Integer> or(Node left, Node right) {
		List<Integer> leftList = left.postingsList;
		List<Integer> rightList = right.postingsList;

		List<Integer> tempList = leftList;
		for (Integer docId : rightList) {
			if (!tempList.contains(docId))
				tempList.add(docId);
		}

		return tempList;
	}

	public List<Integer> and(Node left, Node right) {
		List<Integer> leftList = left.postingsList;
		List<Integer> rightList = right.postingsList;
		List<Integer> tempList = new ArrayList<Integer>();

		if (rightList.size() < leftList.size()) {
			for (Integer docId : rightList) {
				if (leftList.contains(docId))
					tempList.add(docId);
			}
		} else {
			for (Integer docId : leftList) {
				if (rightList.contains(docId))
					tempList.add(docId);
			}
		}
		return tempList;
	}

	public String getIndexDir() {
		return indexDir;
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	public Map<String, Integer> getTermDictionary() {
		return termDictionary;
	}

	public void setTermDictionary(Map<String, Integer> termDictionary) {
		this.termDictionary = termDictionary;
	}

	public Map<Integer, TermData> getTermIndex() {
		return termIndex;
	}

	public void setTermIndex(Map<Integer, TermData> termIndex) {
		this.termIndex = termIndex;
	}

	public Map<Integer, String> getDocumentDictionary() {
		return documentDictionary;
	}

	public void setDocumentDictionary(Map<Integer, String> documentDictionary) {
		this.documentDictionary = documentDictionary;
	}

}
