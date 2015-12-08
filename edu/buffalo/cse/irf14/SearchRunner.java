package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.AuthorAnalyzer;
import edu.buffalo.cse.irf14.analysis.CategoryAnalyzer;
import edu.buffalo.cse.irf14.analysis.ContentAnalyzer;
import edu.buffalo.cse.irf14.analysis.PlaceAnalyzer;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.TermData;
import edu.buffalo.cse.irf14.query.BinaryTree;
import edu.buffalo.cse.irf14.query.Node;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;

/**
 * Main class to run the searcher. As before implement all TODO methods unless
 * marked for bonus
 * 
 * @author chandana
 *
 */
public class SearchRunner {
	public enum ScoringModel {
		TFIDF, OKAPI
	};

	private String indexDir;
	private String corpusDir;
	private PrintStream stream;
	private char mode;
	private int N;
	private double avgDocLen;
	private Map<Integer, String> documentDictionary;
	private Map<String, Integer> termDictionary;
	private Map<Integer, TermData> termIndex;
	private Map<Integer, TermData> invertedIndex;
	private boolean init = true;

	/**
	 * Default (and only public) constuctor
	 * 
	 * @param indexDir
	 *            : The directory where the index resides
	 * @param corpusDir
	 *            : Directory where the (flattened) corpus resides
	 * @param mode
	 *            : Mode, one of Q or E
	 * @param stream
	 *            : Stream to write output to
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public SearchRunner(String indexDir, String corpusDir, char mode,
			PrintStream stream) {
		// TODO: IMPLEMENT THIS METHOD
		this.indexDir = indexDir;
		this.corpusDir = corpusDir;
		this.stream = stream;
		this.mode = mode;

	}

	public void init() {
		try {
			File dir = new File(this.indexDir);
			if (dir.exists()) {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(dir.getAbsolutePath()
								+ File.separator + "Info.ser"));
				String info[] = ((String) ois.readObject()).split(" ");
				this.N = Integer.parseInt(info[0]);
				this.avgDocLen = Double.parseDouble(info[1]);
				ois.close();
			}

			dir = new File(this.indexDir);
			if (dir.exists()) {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(dir.getAbsolutePath()
								+ File.separator + "Term Dictionary.ser"));
				this.termDictionary = (TreeMap<String, Integer>) ois
						.readObject();
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Document Dictionary.ser"));
				this.documentDictionary = (TreeMap<Integer, String>) ois
						.readObject();
				dir = new File(this.indexDir + File.separator + "term");
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Term Index.ser"));
				this.termIndex = (TreeMap<Integer, TermData>) ois.readObject();
				dir = new File(this.indexDir + File.separator + "document");
				ois = new ObjectInputStream(new FileInputStream(
						dir.getAbsolutePath() + File.separator
								+ "Inverted Index.ser"));
				this.invertedIndex = (TreeMap<Integer, TermData>) ois
						.readObject();
				ois.close();
				this.init = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to execute given query in the Q mode
	 * 
	 * @param userQuery
	 *            : Query to be parsed and executed
	 * @param model
	 *            : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) {
		// TODO: IMPLEMENT THIS METHOD
		try {
			long startTime = System.currentTimeMillis();
			Query q = QueryParser.parse(userQuery, "OR");
			List<Integer> resultList = new ArrayList<Integer>();
			if (q != null) {
				String tokenizedQuery = tokenize(q);
				// System.out.println("Query:"+tokenizedQuery);
				if (tokenizedQuery != null) {
					while (this.init)
						init();
					resultList = traverse(tokenizedQuery);
				}

				Map<String, Integer> uniqueResultList = new HashMap<String, Integer>();
				Map<String, Double> rankedResultList = new HashMap<String, Double>();
				if (resultList.size() != 0) {
					for (Integer docId : resultList) {
						uniqueResultList.put(
								this.documentDictionary.get(docId), docId);
					}
					// this.stream.println("Filenames: "+uniqueResultList);

					Map<String, Integer> queryTerms = q.getQueryTerms();

					switch (model) {
					case TFIDF:
						rankedResultList = tfidf(queryTerms, uniqueResultList);
						// System.out.println(rankedResultList);
						break;
					case OKAPI:
						rankedResultList = okapi(queryTerms, uniqueResultList);
						// System.out.println(rankedResultList);
						break;
					}
				}

				File dir = new File(this.indexDir);
				Map<String, String> snippet = new HashMap<String, String>();
				if (dir.exists()) {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(dir.getAbsolutePath()
									+ File.separator + "Snippet.ser"));
					snippet = ((Map<String, String>) ois.readObject());
					ois.close();
				}

				this.stream.println();
				this.stream.println("******************************");
				this.stream.println();

				long endTime = System.currentTimeMillis();

				this.stream.println("Query: " + userQuery);
				this.stream.println("Query time: " + (endTime - startTime)
						+ "ms");
				if (rankedResultList.size() == 0)
					this.stream.println("NO RESULTS FOUND");
				else {
					int rank = 1;
					for (Entry<String, Double> entry : rankedResultList
							.entrySet()) {
						this.stream.println("----------          ----------");
						this.stream.println("Rank: " + rank);
						rank++;
						if (entry.getValue() > 1)
							this.stream.println("Relevance: 1.00000");
						else
							this.stream.println("Relevance: "
									+ entry.getValue());
						String s = snippet.get(entry.getKey());
						String title = s.substring(s.indexOf("Title: "),
								s.indexOf("}"));
						this.stream.println(title);
						String content = s.substring(s.indexOf("Content: "),
								s.length() - 1);
						this.stream.println(content);
					}
				}

				this.stream.println();
				this.stream.println("******************************");
				this.stream.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to execute queries in E mode
	 * 
	 * @param queryFile
	 *            : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		// TODO: IMPLEMENT THIS METHOD
		try {
			int noOfQueries = 0, noOfResults = 0;
			BufferedReader br = new BufferedReader(new FileReader(queryFile));
			String line = br.readLine();
			if (line != null) {
				noOfQueries = Integer
						.parseInt(line.substring(line.indexOf('=') + 1));
			}

			Map<String, String> queries = new HashMap<String, String>();

			while ((line = br.readLine()) != null) {
				String fileId = line.substring(0, line.indexOf(':'));
				String q = line.substring(line.indexOf('{') + 1,
						line.indexOf('}'));
				queries.put(fileId, q);
			}

			Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();

			Iterator<Entry<String, String>> iterator = queries.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				String fileId = entry.getKey();
				String userQuery = entry.getValue();
				// System.out.println(userQuery);
				Query q = QueryParser.parse(userQuery, "OR");
				// System.out.println(q.toString());
				List<Integer> resultList = new ArrayList<Integer>();
				if (q != null) {
					String tokenizedQuery = tokenize(q);
					// System.out.println("Query:"+tokenizedQuery);
					if (tokenizedQuery != null) {
						while (this.init)
							init();
						resultList = traverse(tokenizedQuery);
					}

					Map<String, Integer> uniqueResultList = new HashMap<String, Integer>();
					if (resultList.size() > 0) {
						for (Integer docId : resultList) {
							uniqueResultList.put(
									this.documentDictionary.get(docId), docId);
						}
						// this.stream.println(uniqueResultList);
						Map<String, Integer> queryTerms = q.getQueryTerms();

						Map<String, Double> rankedResultList = okapi(
								queryTerms, uniqueResultList);
						result.put(fileId, rankedResultList);
						// System.out.println(rankedResultList);
					}

				}

			}

			String writeText = "numResults=" + result.size();
			this.stream.println(writeText);

			for (Entry<String, Map<String, Double>> entry : result.entrySet()) {
				String queryId = entry.getKey();
				Map<String, Double> map = entry.getValue();
				writeText = queryId + ":{";
				for (Entry<String, Double> e : map.entrySet()) {
					if (e.getValue() > 1)
						writeText += e.getKey() + "#1.00000, ";
					else
						writeText += e.getKey() + "#" + e.getValue() + ", ";
				}
				writeText = writeText.substring(0, writeText.length() - 2);
				writeText += "}";
				this.stream.println(writeText);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * General cleanup method
	 */
	public void close() {
		// TODO : IMPLEMENT THIS METHOD
		this.stream.close();
	}

	/**
	 * Method to indicate if wildcard queries are supported
	 * 
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		// TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return false;
	}

	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * 
	 * @return A Map containing the original query term as key and list of
	 *         possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		// TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		return null;

	}

	/**
	 * Method to indicate if speel correct queries are supported
	 * 
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		// TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return false;
	}

	/**
	 * Method to get ordered "full query" substitutions for a given misspelt
	 * query
	 * 
	 * @return : Ordered list of full corrections (null if none present) for the
	 *         given query
	 */
	public List<String> getCorrections() {
		// TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		return null;
	}

	public String tokenize(Query q) {

		String query = q.toString();
		query = query.toLowerCase();
		try {
			// System.out.println("Initial:"+query);
			Tokenizer tokenizer = new Tokenizer();
			AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();

			String tempFilter[] = query.split(" ");
			int l = tempFilter.length;
			Map<String, Integer> terms = new HashMap<String, Integer>();

			for (int i = 0; i < tempFilter.length; i++) {
				int j = 0;
				if (tempFilter[i].matches(".*[\"].*")) {
					if (tempFilter[i].matches(".*[\"].*[\"]"))
						break;
					for (j = i + 1; j < l; j++) {
						if (!tempFilter[j].matches(".*[\"].*"))
							tempFilter[i] = tempFilter[i] + " " + tempFilter[j];
						else
							break;

					}
					tempFilter[i] = tempFilter[i] + " " + tempFilter[j];

					l = i + 1;
					// System.out.println(tempFilter[i]+"len:"+l);
					if (l + 1 < tempFilter.length) {
						for (int c = j + 1; c < tempFilter.length; c++) {
							tempFilter[l++] = tempFilter[c];
						}
					} else
						break;
				}
			}

			for (int i = 0; i < l; i++) {
				if (tempFilter[i].contains("term:")
						&& !tempFilter[i].matches(".*\".*\"")) {
					TokenStream contentStream = new TokenStream();
					int flag = 0;
					if (tempFilter[i].matches("[<]term:.*[>]")) {
						flag = 1;
						String r = tempFilter[i];
						r = r.replaceAll("[<]term:", "");

						r = r.replaceAll("[>]", "");
						contentStream.append(tokenizer.consume(r));
					} else {
						String r = tempFilter[i];
						r = r.replaceAll("term:", "");
						contentStream.append(tokenizer.consume(r));
					}

					ContentAnalyzer contentAnalyzer = (ContentAnalyzer) analyzerFactory
							.getAnalyzerForField(FieldNames.CONTENT,
									contentStream);
					contentAnalyzer.increment();
					contentStream = contentAnalyzer.getStream();
					if (contentStream.hasNext()) {
						while (contentStream.hasNext()) {
							Token t = contentStream.next();

							String ts = t.toString();
							if (terms.containsKey(ts)) {
								int count = terms.get(ts) + 1;
								terms.put(ts, count);
							} else {
								terms.put(ts, 1);
							}
							// System.out.println("Stream: "+ts);
							if (flag == 1) {
								tempFilter[i] = "<term:" + ts + ">";
							} else
								tempFilter[i] = "term:" + ts;
						}
					} else
						tempFilter[i] = null;
				} else if (tempFilter[i].contains("category:")
						&& !tempFilter[i].matches(".*\".*\"")) {
					TokenStream categoryStream = new TokenStream();

					int flag = 0;
					if (tempFilter[i].matches("[<]category:.*[>]")) {
						flag = 1;
						String r = tempFilter[i];
						r = r.replaceAll("[<]category:", "");

						r = r.replaceAll("[>]", "");
						categoryStream.append(tokenizer.consume(r));
					} else {
						String r = tempFilter[i];
						r = r.replaceAll("category:", "");
						categoryStream.append(tokenizer.consume(r));
					}

					CategoryAnalyzer categoryAnalyzer = (CategoryAnalyzer) analyzerFactory
							.getAnalyzerForField(FieldNames.CATEGORY,
									categoryStream);
					categoryAnalyzer.increment();
					if (categoryStream.hasNext()) {
						while (categoryStream.hasNext()) {
							Token t = categoryStream.next();

							String ts = t.toString();
							// System.out.println("Stream: "+ts);
							if (flag == 1) {
								tempFilter[i] = "<category:" + ts + ">";
							} else
								tempFilter[i] = "category:" + ts;

						}
					} else
						tempFilter[i] = null;
				} else if (tempFilter[i].contains("author:")
						&& !tempFilter[i].matches(".*\".*\"")) {
					TokenStream authorStream = new TokenStream();
					int flag = 0;
					if (tempFilter[i].matches("[<]author:.*[>]")) {
						flag = 1;
						String r = tempFilter[i];
						r = r.replaceAll("[<]author:", "");

						r = r.replaceAll("[>]", "");
						authorStream.append(tokenizer.consume(r));
					} else {
						String r = tempFilter[i];
						r = r.replaceAll("author:", "");
						authorStream.append(tokenizer.consume(r));
					}
					AuthorAnalyzer authorAnalyzer = (AuthorAnalyzer) analyzerFactory
							.getAnalyzerForField(FieldNames.AUTHOR,
									authorStream);
					authorAnalyzer.increment();
					authorStream = authorAnalyzer.getStream();

					if (authorStream.hasNext()) {
						while (authorStream.hasNext()) {
							Token t = authorStream.next();

							String ts = t.toString();
							// System.out.println("Stream: "+ts);
							if (flag == 1) {
								tempFilter[i] = "<author:" + ts + ">";
							} else
								tempFilter[i] = "author:" + ts;
						}
					} else
						tempFilter[i] = null;
				} else if (tempFilter[i].contains("place:")
						&& !tempFilter[i].matches(".*\".*\"")) {
					TokenStream placeStream = new TokenStream();
					int flag = 0;
					if (tempFilter[i].matches("[<]place:.*[>]")) {
						flag = 1;
						String r = tempFilter[i];
						r = r.replaceAll("[<]place:", "");

						r = r.replaceAll("[>]", "");
						placeStream.append(tokenizer.consume(r));
					} else {
						String r = tempFilter[i];
						r = r.replaceAll("place:", "");
						placeStream.append(tokenizer.consume(r));
					}

					PlaceAnalyzer placeAnalyzer = (PlaceAnalyzer) analyzerFactory
							.getAnalyzerForField(FieldNames.PLACE, placeStream);
					placeAnalyzer.increment();
					placeStream = placeAnalyzer.getStream();
					if (placeStream.hasNext()) {
						while (placeStream.hasNext()) {
							Token t = placeStream.next();

							String ts = t.toString();
							// System.out.println("Stream: "+ts);

							if (flag == 1) {
								tempFilter[i] = "<place:" + ts + ">";
							} else
								tempFilter[i] = "place:" + ts;
						}
					} else
						tempFilter[i] = null;
				}
			}

			for (int i = 0; i < l; i++) {
				if (tempFilter[i].matches("category:\".*\"")) {
					int j = 0;
					tempFilter[i] = tempFilter[i].replaceAll("\"", "");
					tempFilter[i] = tempFilter[i].replaceAll("category:", "");
					String w[] = tempFilter[i].split(" ");
					tempFilter[i] = "";
					for (j = 0; j < w.length - 1; j++) {
						tempFilter[i] = tempFilter[i] + "category:" + w[j]
								+ " and ";
					}
					tempFilter[i] = tempFilter[i] + "category:" + w[j];
					// System.out.println(tempFilter[i]);
				} else if (tempFilter[i].matches("author:\".*\"")) {
					int j = 0;
					tempFilter[i] = tempFilter[i].replaceAll("\"", "");
					tempFilter[i] = tempFilter[i].replaceAll("author:", "");
					String w[] = tempFilter[i].split(" ");
					tempFilter[i] = "";
					for (j = 0; j < w.length - 1; j++) {
						tempFilter[i] = tempFilter[i] + "author:" + w[j]
								+ " and ";
					}
					tempFilter[i] = tempFilter[i] + "author:" + w[j];
					// System.out.println(tempFilter[i]);
				} else if (tempFilter[i].matches("place:\".*\"")) {
					int j = 0;
					tempFilter[i] = tempFilter[i].replaceAll("\"", "");
					tempFilter[i] = tempFilter[i].replaceAll("place:", "");
					String w[] = tempFilter[i].split(" ");
					tempFilter[i] = "";
					for (j = 0; j < w.length - 1; j++) {
						tempFilter[i] = tempFilter[i] + "place:" + w[j]
								+ " and ";
					}
					tempFilter[i] = tempFilter[i] + "place:" + w[j];
					// System.out.println(tempFilter[i]);
				} else if (tempFilter[i].matches("term:\".*\"")) {
					int j = 0;
					tempFilter[i] = tempFilter[i].replaceAll("\"", "");
					tempFilter[i] = tempFilter[i].replaceAll("term:", "");
					String w[] = tempFilter[i].split(" ");
					tempFilter[i] = "";
					for (j = 0; j < w.length - 1; j++) {
						tempFilter[i] = tempFilter[i] + "term:" + w[j]
								+ " and ";
					}
					tempFilter[i] = tempFilter[i] + "term:" + w[j];
					// System.out.println(tempFilter[i]);
				}

			}

			for (int i = 0; i < l; i++) {
				if (tempFilter[i] == null) {
					if (i - 1 >= 1) {
						if (tempFilter[i - 1].toLowerCase().matches("and|or")) {
							tempFilter[i] = "";
							tempFilter[i - 1] = "";
						} else {
							if (i + 1 < l)
								if (tempFilter[i + 1].toLowerCase().matches(
										"and|or")) {
									tempFilter[i] = "";
									tempFilter[i + 1] = "";
								}
						}
					} else {
						if (i + 1 < l)
							if (tempFilter[i + 1].toLowerCase().matches(
									"and|or")) {
								tempFilter[i] = "";
								tempFilter[i + 1] = "";
							}
					}
					if (i + 1 < l)
						if (tempFilter[i + 1].contentEquals("]")) {

						}
				}
			}

			String newquery = "";
			for (int i = 0; i < l; i++)
				newquery = newquery + tempFilter[i] + " ";
			newquery = newquery.replaceAll("\\s+", " ").trim();
			// System.out.println(newquery);

			String temp[] = newquery.split(" ");

			l = temp.length;

			for (int i = 0; i < temp.length; i++) {
				int j = 0;
				if (temp[i].matches(".*[\"].*")) {
					if (temp[i].matches(".*[\"].*[\"]"))
						break;
					for (j = i + 1; j < l; j++) {
						if (!temp[j].matches(".*[\"].*"))
							temp[i] = temp[i] + " " + temp[j];
						else
							break;

					}
					temp[i] = temp[i] + " " + temp[j];

					l = i + 1;
					// System.out.println(temp[i]+"len:"+l);
					if (l + 1 < temp.length) {
						for (int c = j + 1; c < temp.length; c++) {
							temp[l++] = temp[c];
						}
					} else
						break;
				}

			}

			for (int i = 0; i < l; i++) {
				if (temp[i].contentEquals("[")) {
					if (i + 1 < l)
						if (temp[i + 1].toLowerCase().matches(".*term:.*")
								|| temp[i + 1].toLowerCase().matches(
										".*category:.*")
								|| temp[i + 1].toLowerCase().matches(
										".*place:.*")
								|| temp[i + 1].toLowerCase().matches(
										".*author:.*"))
							if (i + 2 < l)
								if (temp[i + 2].contentEquals("]")) {
									temp[i] = "";
									temp[i + 2] = "";

								}
				}
			}

			newquery = "";
			for (int i = 0; i < l; i++)
				newquery = newquery + temp[i] + " ";
			newquery = newquery.replaceAll("\\s+", " ").trim();
			// System.out.println("FINAL:"+newquery);
			q.setQueryTerms(terms);
			return newquery;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return query;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Integer> traverse(String newquery) {
		String tempTree[] = newquery.split(" ");

		int l = tempTree.length;
		int i = 0;
		for (i = 0; i < tempTree.length; i++) {
			int j = 0;
			if (tempTree[i].matches(".*[\"].*")) {
				if (tempTree[i].matches(".*[\"].*[\"]"))
					break;
				for (j = i + 1; j < l; j++) {
					if (!tempTree[j].matches(".*[\"].*"))
						tempTree[i] = tempTree[i] + " " + tempTree[j];
					else
						break;

				}
				tempTree[i] = tempTree[i] + " " + tempTree[j];

				l = i + 1;
				if (l + 1 < tempTree.length) {
					for (int c = j + 1; c < tempTree.length; c++) {
						tempTree[l++] = tempTree[c];
					}
				} else
					break;
			}
		}

		// System.out.println("--------------------------");
		for (i = 0; i < l; i++) {
			// System.out.println(tempTree[i]);
		}

		BinaryTree t = new BinaryTree();
		t.setIndexDir(this.indexDir);
		t.setTermDictionary(this.termDictionary);
		t.setTermIndex(this.termIndex);
		t.setDocumentDictionary(this.documentDictionary);

		Stack operands = new Stack();
		Stack operators = new Stack();

		Node root = t.root;
		Node left, r, right;
		// System.out.println(l);
		if (l >= 5) {
			for (i = 0; i < l; i++) {

				if (tempTree[i].contentEquals("]")
						|| tempTree[i].contentEquals("}")) {
					if (tempTree[i].contentEquals("}")) {
						Node tempnode = (Node) operands.pop();
						Node braces = (Node) operands.pop();
						String check = (String) braces.name;
						if (check.contentEquals("{")) {
							operands.push(braces);
							operands.push(tempnode);
							break;
						} else {
							operands.push(braces);
							operands.push(tempnode);
						}
					}
					String tempstring = null;
					Node tempnode;
					while (!operands.isEmpty() && !operators.isEmpty()) {
						left = (Node) operands.pop();
						right = (Node) operands.pop();
						r = (Node) operators.pop();
						root = t.addNode(left, r, right);
						if (!operands.isEmpty()) {
							tempnode = (Node) operands.pop();
							tempstring = tempnode.name;
							if (tempstring.contentEquals("[")
									|| tempstring.contentEquals("{"))
								break;
							else
								operands.push(tempnode);
						}

						operands.push(root);
					}
					operands.push(root);

				} else if (!tempTree[i].equalsIgnoreCase("AND")
						&& !tempTree[i].equalsIgnoreCase("OR")) {
					Node z = new Node(tempTree[i]);
					operands.push(z);
					// System.out.println("operands"+i+":"+z.name);
				}

				else {
					operators.push(new Node(tempTree[i]));
					// System.out.println("operators"+i+":"+tempTree[i]);

				}

			}
			t.root = (Node) operands.pop();
		} else {
			if (l == 3) {
				if (tempTree[0].contentEquals("{")
						&& tempTree[2].contentEquals("}"))
					t.root = new Node(tempTree[1]);
			} else {
				t.root = null;
				System.out.println(l);
			}
		}

		// System.out.println("INORDER");

		t.inOrderTraverseTree(t.root);
		// this.stream.println("Postings list: "+t.root.postingsList);
		return t.root.postingsList;
	}

	public Map<String, Double> tfidf(Map<String, Integer> queryTerms,
			Map<String, Integer> uniqueResultList) {
		Map<String, Double> resultList = new HashMap<String, Double>();
		Map<String, Double> rankedResultList = new LinkedHashMap<String, Double>();

		// idf values for all query terms
		Map<String, Double> idf = new HashMap<String, Double>();
		int totalQueryTF = 0;
		for (Entry<String, Integer> entry : queryTerms.entrySet()) {
			int tf = entry.getValue();
			totalQueryTF += tf * tf;
			String term = entry.getKey();
			if (this.termDictionary.get(term) != null) {
				TermData termData = this.termIndex.get(this.termDictionary
						.get(term));
				idf.put(term, termData.getIdf());
			} else
				idf.put(term, 0.0);
		}

		// query vector
		double queryED = Math.sqrt(totalQueryTF);
		double queryVector[] = new double[queryTerms.size()];
		int i = 0;
		for (Entry<String, Integer> entry : queryTerms.entrySet()) {
			String term = entry.getKey();
			int tf = entry.getValue();
			double normalizedTF = tf / queryED;
			queryVector[i] = normalizedTF * idf.get(term);
			i++;
		}

		// all document vectors
		Map<String, Double[]> docVector = new HashMap<String, Double[]>();

		for (Entry<String, Integer> doc : uniqueResultList.entrySet()) {
			int totalDocTF = 0;
			Integer docId = doc.getValue();
			TermData termData = this.invertedIndex.get(docId);
			for (Entry<Integer, Integer> entry : termData.getTermFrequency()
					.entrySet()) {
				int tf = entry.getValue();
				totalDocTF += tf * tf;
			}

			double docED = Math.sqrt(totalDocTF);
			Double singleDocVector[] = new Double[queryTerms.size()];
			i = 0;
			for (Entry<String, Integer> entry : queryTerms.entrySet()) {
				String term = entry.getKey();
				int tf = 0;
				if (this.termDictionary.get(term) != null)
					if (this.invertedIndex.get(docId).getTermFrequency()
							.get(this.termDictionary.get(term)) != null)
						tf = this.invertedIndex.get(docId).getTermFrequency()
								.get(this.termDictionary.get(term));
				double normalizedTF = tf / docED;

				singleDocVector[i] = normalizedTF * idf.get(term);
				i++;
			}
			docVector.put(doc.getKey(), singleDocVector);
		}

		// cosine similarity
		for (Entry<String, Double[]> entry : docVector.entrySet()) {
			Double dVector[] = entry.getValue();
			double dotProduct = 0.0;
			double dv = 0.0;
			double q = 0.0;
			for (i = 0; i < queryVector.length; i++) {
				dotProduct += dVector[i] * queryVector[i];
				dv += dVector[i] * dVector[i];
				q += queryVector[i] * queryVector[i];
			}

			dv = (double) Math.sqrt(dv);
			q = (double) Math.sqrt(q);

			/*
			 * BigDecimal num = new BigDecimal(dotProduct); num =
			 * num.setScale(10, BigDecimal.ROUND_HALF_UP); BigDecimal den1 = new
			 * BigDecimal(dv); den1 = den1.setScale(10,
			 * BigDecimal.ROUND_HALF_UP); BigDecimal den2 = new BigDecimal(q);
			 * den2 = den2.setScale(10, BigDecimal.ROUND_HALF_UP); BigDecimal
			 * den = den1.multiply(den2); num = num.setScale(10,
			 * BigDecimal.ROUND_HALF_UP); BigDecimal answer = num.divide(den, 5,
			 * BigDecimal.ROUND_HALF_UP);
			 */
			Double answer = dotProduct / (dv * q);
			// System.out.println("answer "+answer);
			if (answer.isNaN())
				resultList.put(entry.getKey(), 1.0);
			else {
				BigDecimal a = BigDecimal.valueOf(answer).setScale(5,
						BigDecimal.ROUND_HALF_UP);
				resultList.put(entry.getKey(), a.doubleValue());
			}
			// resultList.put(entry.getKey(), answer);
		}

		Iterator<Entry<String, Double>> iterator = entriesSortedByValues(
				resultList).iterator();
		int count = 0;
		while (count < 10 && iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			rankedResultList.put(entry.getKey(), entry.getValue());
			count++;
		}

		return rankedResultList;
	}

	public Map<String, Double> okapi(Map<String, Integer> queryTerms,
			Map<String, Integer> uniqueResultList) {
		Map<String, Double> resultList = new HashMap<String, Double>();
		Map<String, Double> rankedResultList = new LinkedHashMap<String, Double>();

		Double idf[] = new Double[queryTerms.size()];
		int i = 0;
		for (Entry<String, Integer> entry : queryTerms.entrySet()) {
			String term = entry.getKey();
			if (this.termDictionary.get(term) != null) {
				TermData termData = this.termIndex.get(this.termDictionary
						.get(term));
				idf[i] = termData.getIdf();
			} else
				idf[i] = 0.0;
			i++;
		}

		double k1 = 1.2;
		double k3 = 8.0;
		double b = 0.75;

		// query vector
		int queryVector[] = new int[queryTerms.size()];
		i = 0;
		for (Entry<String, Integer> entry : queryTerms.entrySet()) {
			int tf = entry.getValue();
			queryVector[i] = tf;
			i++;
		}

		// all document vectors
		Map<Integer, Integer[]> docVector = new HashMap<Integer, Integer[]>();
		for (Entry<String, Integer> doc : uniqueResultList.entrySet()) {
			int docId = doc.getValue();
			TermData termData = this.invertedIndex.get(docId);
			Integer singleDocVector[] = new Integer[queryTerms.size()];
			i = 0;
			for (Entry<String, Integer> qt : queryTerms.entrySet()) {
				String term = qt.getKey();
				int tf = 0;
				if (this.termDictionary.get(term) != null)
					if (this.invertedIndex.get(docId).getTermFrequency()
							.get(this.termDictionary.get(term)) != null)
						tf = this.invertedIndex.get(docId).getTermFrequency()
								.get(this.termDictionary.get(term));
				singleDocVector[i] = tf;
				i++;
			}

			docVector.put(docId, singleDocVector);

		}

		int length = 0;
		for (Entry<Integer, Integer[]> entry : docVector.entrySet()) {
			Integer dVector[] = entry.getValue();
			int docId = entry.getKey();
			double rsv = 0.0;
			for (i = 0; i < queryVector.length; i++) {
				int tfd = dVector[i];
				int tfq = queryVector[i];
				int ld = this.invertedIndex.get(docId).getDocumentLength();
				double IDF = idf[i];
				double secondTerm = ((k1 + 1) * tfd)
						/ (k1 * ((1 - b) + b * (ld / this.avgDocLen)) + tfd);
				double thirdTerm = (k3 + 1) * tfq / (k3 + tfq);
				rsv += IDF * secondTerm * thirdTerm;
			}

			int temp = (int) rsv;
			if (temp != 0) {
				int l = String.valueOf(temp).length();
				if (l > length)
					length = l;
			}

			BigDecimal a = BigDecimal.valueOf(rsv).setScale(5,
					BigDecimal.ROUND_HALF_UP);
			resultList.put(this.documentDictionary.get(docId), a.doubleValue());
		}

		Iterator<Entry<String, Double>> iterator = entriesSortedByValues(
				resultList).iterator();
		int count = 0;
		while (count < 10 && iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			if (length != 0) {
				BigDecimal a = BigDecimal.valueOf(
						entry.getValue() / (10 * length)).setScale(5,
						BigDecimal.ROUND_HALF_UP);
				rankedResultList.put(entry.getKey(), a.doubleValue());
			} else
				rankedResultList.put(entry.getKey(), entry.getValue());
			count++;
		}

		return rankedResultList;
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
