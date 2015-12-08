package edu.buffalo.cse.irf14.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author chandana 
 * Class that emulates reading data back from a written index
 */
public class IndexReader {

	private String indexDir;
	private IndexType type;

	/**
	 * Default constructor
	 * 
	 * @param indexDir
	 *            : The root directory from which the index is to be read. This
	 *            will be exactly the same directory as passed on IndexWriter.
	 *            In case you make subdirectories etc., you will have to handle
	 *            it accordingly.
	 * @param type
	 *            The {@link IndexType} to read from
	 */
	public IndexReader(String indexDir, IndexType type) {
		// TODO
		this.indexDir = indexDir;
		this.type = type;
	}

	/**
	 * Get total number of terms from the "key" dictionary associated with this
	 * index. A postings list is always created against the "key" dictionary
	 * 
	 * @return The total number of terms
	 */
	@SuppressWarnings({ "unchecked" })
	public int getTotalKeyTerms() {
		// TODO : YOU MUST IMPLEMENT THIS
		File dir = new File(this.indexDir);
		ObjectInputStream ois;
		try {
			if (dir.exists()) {
				switch (this.type) {
				case TERM:
					ois = new ObjectInputStream(new FileInputStream(
							dir.getAbsolutePath() + File.separator
									+ "Term Dictionary.ser"));
					Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
							.readObject();
					ois.close();
					return dictionary.size();
				case AUTHOR:
					ois = new ObjectInputStream(new FileInputStream(
							dir.getAbsolutePath() + File.separator
									+ "Author Dictionary.ser"));
					Map<String, String> authorDictionary = (TreeMap<String, String>) ois
							.readObject();
					ois.close();
					return authorDictionary.size();
				case CATEGORY:
					ois = new ObjectInputStream(new FileInputStream(
							dir.getAbsolutePath() + File.separator
									+ "Category Dictionary.ser"));
					dictionary = (TreeMap<String, Integer>) ois.readObject();
					ois.close();
					return dictionary.size();
				case PLACE:
					ois = new ObjectInputStream(new FileInputStream(
							dir.getAbsolutePath() + File.separator
									+ "Place Dictionary.ser"));
					dictionary = (TreeMap<String, Integer>) ois.readObject();
					ois.close();
					return dictionary.size();
				default:
					return -1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * Get total number of terms from the "value" dictionary associated with
	 * this index. A postings list is always created with the "value" dictionary
	 * 
	 * @return The total number of terms
	 */
	@SuppressWarnings("unchecked")
	public int getTotalValueTerms() {
		// TODO: YOU MUST IMPLEMENT THIS
		try {
			File dir = new File(this.indexDir);
			if (dir.exists()) {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(dir.getAbsolutePath()
								+ File.separator + "Document Dictionary.ser"));
				Map<Integer, String> dictionary = (TreeMap<Integer, String>) ois
						.readObject();
				ois.close();
				return dictionary.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * Method to get the postings for a given term. You can assume that the raw
	 * string that is used to query would be passed through the same Analyzer as
	 * the original field would have been.
	 * 
	 * @param term
	 *            : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the
	 *         number of occurrences as values if the given term was found, null
	 *         otherwise.
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public Map<String, Integer> getPostings(String term) {
		if (term != null) {
			try {
				File dir = new File(this.indexDir);
				Map<String, Integer> map;
				if (dir.exists()) {
					ObjectInputStream ois;
					switch (this.type) {
					case TERM:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Term Dictionary.ser"));
						Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator + "term");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Term Index.ser"));
						map = postings(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, term);
						ois.close();
						return map;
					case AUTHOR:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Author Dictionary.ser"));
						Map<String, String> authorDictionary = (TreeMap<String, String>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator
								+ "author");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Author Index.ser"));
						map = authorPostings(
								(TreeMap<String, List<Integer>>) ois
										.readObject(),
								authorDictionary, term);
						ois.close();
						return map;
					case CATEGORY:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Category Dictionary.ser"));
						dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator
								+ "category");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Category Index.ser"));
						map = postings(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, term);
						ois.close();
						return map;
					case PLACE:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Place Dictionary.ser"));
						dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator + "place");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Place Index.ser"));
						map = postings(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, term);
						ois.close();
						return map;
					default:
						return null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * 
	 * @param k
	 *            : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k for valid k values null
	 *         for invalid k values
	 */
	@SuppressWarnings({ "resource", "unchecked" })
	public List<String> getTopK(int k) {
		// TODO YOU MUST IMPLEMENT THIS
		if (k > 0) {
			try {
				File dir = new File(this.indexDir);
				List<String> list;
				if (dir.exists()) {
					ObjectInputStream ois;
					switch (this.type) {
					case TERM:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Term Dictionary.ser"));
						Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator + "term");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Term Index.ser"));
						list = sort(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, k);
						ois.close();
						return list;
					case AUTHOR:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Author Dictionary.ser"));
						Map<String, String> authorDictionary = (TreeMap<String, String>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator
								+ "author");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Author Index.ser"));
						list = sortAuthor(
								(TreeMap<String, List<Integer>>) ois
										.readObject(),
								authorDictionary, k);
						ois.close();
						return list;
					case CATEGORY:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Category Dictionary.ser"));
						dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator
								+ "category");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Category Index.ser"));
						list = sort(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, k);
						ois.close();
						return list;
					case PLACE:
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Place Dictionary.ser"));
						dictionary = (TreeMap<String, Integer>) ois
								.readObject();
						dir = new File(this.indexDir + File.separator + "place");
						ois = new ObjectInputStream(new FileInputStream(
								dir.getAbsolutePath() + File.separator
										+ "Place Index.ser"));
						list = sort(
								(TreeMap<Integer, TermData>) ois.readObject(),
								dictionary, k);
						ois.close();
						return list;
					default:
						return null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Method to implement a simple boolean AND query on the given index
	 * 
	 * @param terms
	 *            The ordered set of terms to AND, similar to getPostings() the
	 *            terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key and
	 *         number of occurrences as the value, the number of occurrences
	 *         would be the sum of occurrences for each participating term.
	 *         return null if the given term list returns no results BONUS ONLY
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public Map<String, Integer> query(String... terms) {
		// TODO : BONUS ONLY
		// get postings list for each term->find overlap->find sum of
		// occurrences of terms
		try {
			File dir = new File(this.indexDir);
			Map<String, List<Integer>> postings = new TreeMap<String, List<Integer>>();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					dir.getAbsolutePath() + File.separator
							+ "Term Dictionary.ser"));
			Map<String, Integer> dictionary = (TreeMap<String, Integer>) ois
					.readObject();
			dir = new File(this.indexDir + File.separator + "term");
			ois = new ObjectInputStream(new FileInputStream(
					dir.getAbsolutePath() + File.separator + "Term Index.ser"));
			Map<Integer, TermData> index = (Map<Integer, TermData>) ois
					.readObject();
			Map<Integer, Integer> uniqueFileList = new HashMap<Integer, Integer>();
			for (String term : terms) {
				Map<Integer, Integer> termFrequency = index.get(
						dictionary.get(term)).getTermFrequency();
				Iterator<Entry<Integer, Integer>> tfIterator = termFrequency
						.entrySet().iterator();
				List<Integer> tempPostings = new ArrayList<Integer>();
				while (tfIterator.hasNext())
					tempPostings.add(tfIterator.next().getKey());

				postings.put(term, tempPostings);
				Iterator<Integer> i = tempPostings.iterator();
				while (i.hasNext()) {
					uniqueFileList.put((Integer) i.next(), 5);
				}
			}

			Iterator<Entry<Integer, Integer>> fileIterator = uniqueFileList
					.entrySet().iterator();
			Map<Integer, Integer> fileList = new HashMap<Integer, Integer>();
			while (fileIterator.hasNext()) {
				Integer fileId = fileIterator.next().getKey();
				boolean flag = true;
				Integer count = 0;
				Iterator<Entry<String, List<Integer>>> postingsIterator = postings
						.entrySet().iterator();
				while (postingsIterator.hasNext()) {
					List<Integer> temp = postingsIterator.next().getValue();
					if (!temp.contains(fileId)) {
						flag = false;
						break;
					} else {
						int frequency = 0;
						for (Integer i : temp) {
							if (i.equals(fileId)) {
								frequency++;
							}
						}
						count += frequency;
					}

				}

				if (flag)
					fileList.put(fileId, count);
			}

			Map<String, Integer> result = new HashMap<String, Integer>();

			dir = new File(this.indexDir);
			if (dir.exists()) {
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Document Dictionary.ser"));
				Map<Integer, String> documentDictionary = (TreeMap<Integer, String>) ois
						.readObject();
				Iterator<Entry<Integer, Integer>> i2 = fileList.entrySet()
						.iterator();
				while (i2.hasNext()) {
					Entry<Integer, Integer> entry = i2.next();
					result.put(documentDictionary.get(entry.getKey()),
							entry.getValue());
				}
			}

			ois.close();

			if (result.size() > 0)
				return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> sort(Map<Integer, TermData> index,
			Map<String, Integer> dictionary, int k) {
		List<String> list = new ArrayList<String>();
		Map<String, Integer> temp = new HashMap<String, Integer>();

		Iterator<Entry<String, Integer>> i1 = dictionary.entrySet().iterator();
		while (i1.hasNext()) {
			Entry<String, Integer> entry = i1.next();
			TermData data = index.get(entry.getValue());
			Map<Integer, Integer> termFrequency = data.getTermFrequency();
			Iterator<Entry<Integer, Integer>> tfIterator = termFrequency
					.entrySet().iterator();
			int count = 0;
			while (tfIterator.hasNext())
				count += tfIterator.next().getValue();

			temp.put(entry.getKey(), count);
		}

		Iterator<Entry<String, Integer>> i2 = entriesSortedByValues(temp)
				.iterator();
		while (k > 0) {
			Entry<String, Integer> entry = i2.next();
			list.add(entry.getKey());
			k--;
		}
		return list;
	}

	public List<String> sortAuthor(Map<String, List<Integer>> index,
			Map<String, String> dictionary, int k) {
		List<String> list = new ArrayList<String>();
		Map<String, Integer> temp = new HashMap<String, Integer>();

		Iterator<Entry<String, String>> i1 = dictionary.entrySet().iterator();
		while (i1.hasNext()) {
			Entry<String, String> entry = i1.next();
			temp.put(entry.getKey(), index.get(entry.getValue()).size());
		}

		Iterator<Entry<String, Integer>> i2 = entriesSortedByValues(temp)
				.iterator();
		while (k > 0) {
			Entry<String, Integer> entry = i2.next();
			list.add(entry.getKey());
			k--;
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Integer> authorPostings(
			Map<String, List<Integer>> index, Map<String, String> dictionary,
			String term) {
		try {
			Map<String, Integer> temp = new HashMap<String, Integer>();

			Map<Integer, Integer> temp1 = new HashMap<Integer, Integer>();
			// get postings->calculate frequency of doc in list->document
			// dictionary->get filename->put in map
			List<Integer> postings = index.get(term);
			if (postings != null) {
				Iterator<Integer> i1 = postings.iterator();
				while (i1.hasNext()) {
					int fileId = i1.next();
					if (temp.containsKey(fileId))
						temp1.put(fileId, temp.get(fileId) + 1);
					else
						temp1.put(fileId, 1);
				}

				File dir = new File(this.indexDir);
				if (dir.exists()) {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(dir.getAbsolutePath()
									+ File.separator
									+ "Document Dictionary.ser"));
					Map<Integer, String> documentDictionary = (TreeMap<Integer, String>) ois
							.readObject();
					ois.close();
					Iterator<Entry<Integer, Integer>> i2 = temp1.entrySet()
							.iterator();
					while (i2.hasNext()) {
						Entry<Integer, Integer> entry = i2.next();
						temp.put(documentDictionary.get(entry.getKey()),
								entry.getValue());
					}
				}
				return temp;
			}

			return null;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Integer> postings(Map<Integer, TermData> index,
			Map<String, Integer> dictionary, String term) {
		try {
			Map<String, Integer> temp = new HashMap<String, Integer>();

			// get postings->calculate frequency of doc in list->document
			// dictionary->get filename->put in map
			if (dictionary.get(term) != null) {
				TermData termData = index.get(dictionary.get(term));

				Map<Integer, Integer> postings = termData.getTermFrequency();

				File dir = new File(this.indexDir);
				if (dir.exists()) {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(dir.getAbsolutePath()
									+ File.separator
									+ "Document Dictionary.ser"));
					Map<Integer, String> documentDictionary = (TreeMap<Integer, String>) ois
							.readObject();
					ois.close();
					Iterator<Entry<Integer, Integer>> i1 = postings.entrySet()
							.iterator();
					while (i1.hasNext()) {
						Entry<Integer, Integer> entry = i1.next();
						temp.put(documentDictionary.get(entry.getKey()),
								entry.getValue());
					}
				}
				return temp;
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e2.getValue().compareTo(e1.getValue());
						return res != 0 ? res : 1; // Special fix to preserve
													// items with equal values
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}
}
