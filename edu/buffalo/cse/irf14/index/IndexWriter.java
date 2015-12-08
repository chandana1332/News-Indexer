package edu.buffalo.cse.irf14.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.AuthorAnalyzer;
import edu.buffalo.cse.irf14.analysis.CategoryAnalyzer;
import edu.buffalo.cse.irf14.analysis.ContentAnalyzer;
import edu.buffalo.cse.irf14.analysis.NewsDateAnalyzer;
import edu.buffalo.cse.irf14.analysis.PlaceAnalyzer;
import edu.buffalo.cse.irf14.analysis.TitleAnalyzer;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author chandana 
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {

	private String indexDir;
	private int count;
	private int currentFileId;
	private String currentAuthorOrg;
	private Map<Integer, String> documentDictionary;
	private Map<String, String> snippet;
	private Map<String, Integer> termDictionary;
	private Map<Integer, TermData> termIndex;
	private Map<String, Integer> categoryDictionary;
	private Map<Integer, TermData> categoryIndex;
	private Map<String, Integer> placeDictionary;
	private Map<Integer, TermData> placeIndex;
	private Map<String, String> authorDictionary;
	private Map<String, List<Integer>> authorIndex;
	private Map<Integer, TermData> invertedIndex;

	private int N;
	private double avgDocLen;

	// This parameter contains the address of the folder where everything needs
	// to be stored.
	/**
	 * Default constructor
	 * 
	 * @param indexDir
	 *            : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		// TODO : YOU MUST IMPLEMENT THIS
		// If there is a parameter passed, assign it to the variable.
		this.indexDir = indexDir;
		File dir = new File(indexDir);
		if (!dir.exists())
			dir.mkdir();
		else {
			String contents[] = dir.list();
			File folder;
			for (String content : contents) {
				folder = new File(indexDir + File.separator + content);
				if (folder.isDirectory()) {
					String files[] = folder.list();
					for (String file : files) {
						new File(folder.getAbsolutePath() + File.separator
								+ file).delete();
					}
				}
				folder.delete();
			}
		}
		this.documentDictionary = new TreeMap<Integer, String>();
		this.termDictionary = new TreeMap<String, Integer>();
		this.termIndex = new TreeMap<Integer, TermData>();
		this.categoryDictionary = new TreeMap<String, Integer>();
		this.categoryIndex = new TreeMap<Integer, TermData>();
		this.placeDictionary = new TreeMap<String, Integer>();
		this.placeIndex = new TreeMap<Integer, TermData>();
		this.authorDictionary = new TreeMap<String, String>();
		this.authorIndex = new TreeMap<String, List<Integer>>();
		this.invertedIndex = new TreeMap<Integer, TermData>();
		this.count = 1;
		this.snippet = new TreeMap<String, String>();

		this.N = 0;
		this.avgDocLen = 0.0;
	}

	public String getIndexDir() {
		return indexDir;
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	/**
	 * Method to add the given Document to the index This method should take
	 * care of reading the filed values, passing them through corresponding
	 * analyzers and then indexing the results for each indexable field within
	 * the document.
	 * 
	 * @param d
	 *            : The Document to be added
	 * @throws IndexerException
	 *             : In case any error occurs
	 * @throws TokenizerException
	 */
	public void addDocument(Document d) throws IndexerException {
		// TODO : YOU MUST IMPLEMENT THIS
		// Here we add a document for indexing so before we add it we need to
		// tokenize it and filter it and that filtered one will be added.
		// Hence we call Tokenizer where the delim is set to default or to the
		// specified one
		Tokenizer tokenizer = new Tokenizer();
		TokenStream fileIdStream = new TokenStream();
		TokenStream categoryStream = new TokenStream();
		TokenStream contentStream = new TokenStream();
		TokenStream authorStream = new TokenStream();
		// TokenStream authorOrgStream = new TokenStream();
		TokenStream placeStream = new TokenStream();
		TokenStream dateStream = new TokenStream();
		TokenStream titleStream = new TokenStream();
		TokenStream termStream = new TokenStream();

		try {
			String fileids[] = d.getField(FieldNames.FILEID);

			if (fileids != null) {
				for (String fileid : fileids) {
					fileIdStream.append(tokenizer.consume(fileid));
				}
			}

			String categories[] = d.getField(FieldNames.CATEGORY);
			if (categories != null) {
				for (String category : categories) {
					categoryStream.append(tokenizer.consume(category));
				}
			}

			String titles[] = d.getField(FieldNames.TITLE);
			String snip = "{Title: " + titles[0] + "}";
			if (titles != null) {
				for (String title : titles) {
					titleStream.append(tokenizer.consume(title));
				}
			}

			String authors[] = d.getField(FieldNames.AUTHOR);
			if (authors != null) {
				for (String author : authors) {
					authorStream.append(tokenizer.consume(author));
				}
			}

			String authorOrgs[] = d.getField(FieldNames.AUTHORORG);
			if (authorOrgs != null) {
				for (String authorOrg : authorOrgs) {
					this.currentAuthorOrg = authorOrg;
				}
			}

			String places[] = d.getField(FieldNames.PLACE);
			if (places != null) {
				for (String place : places) {
					placeStream.append(tokenizer.consume(place));
				}
			}

			String newsDate[] = d.getField(FieldNames.NEWSDATE);
			if (newsDate != null) {
				for (String newsdate : newsDate) {
					dateStream.append(tokenizer.consume(newsdate));
				}
			}

			String content[] = d.getField(FieldNames.CONTENT);
			if (content[0].length() > 125)
				snip += "{Content: " + content[0].substring(0, 125) + "...}";
			else
				snip += "{Content: " + content[0] + "...}";

			this.snippet.put(fileids[0], snip);
			if (content != null) {
				for (String con : content) {
					contentStream.append(tokenizer.consume(con));
				}
			}
			// System.out.println(snip);
			AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();

			CategoryAnalyzer categoryAnalyzer = (CategoryAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.CATEGORY, categoryStream);
			categoryAnalyzer.increment();
			categoryStream = categoryAnalyzer.getStream();

			TitleAnalyzer titleAnalyzer = (TitleAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.TITLE, titleStream);
			titleAnalyzer.increment();
			titleStream = titleAnalyzer.getStream();

			AuthorAnalyzer authorAnalyzer = (AuthorAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.AUTHOR, authorStream);
			authorAnalyzer.increment();
			authorStream = authorAnalyzer.getStream();

			/*
			 * AuthorOrgAnalyzer authorOrgAnalyzer = (AuthorOrgAnalyzer)
			 * analyzerFactory.getAnalyzerForField(FieldNames.AUTHORORG,
			 * authorOrgStream);
			 * authorOrgAnalyzer.setTokenFilterFactory(tokenFactory);
			 * authorOrgAnalyzer.increment(); authorOrgStream =
			 * authorOrgAnalyzer.getStream();
			 */

			PlaceAnalyzer placeAnalyzer = (PlaceAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.PLACE, placeStream);
			placeAnalyzer.increment();
			placeStream = placeAnalyzer.getStream();

			NewsDateAnalyzer newsDateAnalyzer = (NewsDateAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.NEWSDATE, dateStream);
			newsDateAnalyzer.increment();
			dateStream = newsDateAnalyzer.getStream();

			ContentAnalyzer contentAnalyzer = (ContentAnalyzer) analyzerFactory
					.getAnalyzerForField(FieldNames.CONTENT, contentStream);
			contentAnalyzer.increment();
			contentStream = contentAnalyzer.getStream();

			termStream.append(titleStream);
			termStream.append(dateStream);
			termStream.append(contentStream);

			addToDocumentDictionary(fileIdStream);

			addToCategoryDictionary(categoryStream);
			categoryStream.reset();
			addToCategoryIndex(categoryStream);

			addToPlaceDictionary(placeStream);
			placeStream.reset();
			addToPlaceIndex(placeStream);

			addToAuthorDictionary(authorStream);
			authorStream.reset();
			addToAuthorIndex(authorStream);

			addToTermDictionary(termStream);
			termStream.reset();
			addToTermIndex(termStream);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void addToDocumentDictionary(TokenStream fileIdStream) {
		try {
			while (fileIdStream.hasNext()) {
				this.currentFileId = this.documentDictionary.size() + 1;
				this.documentDictionary.put(this.currentFileId, fileIdStream
						.next().toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	private void writeToDocumentDictionary() {
		try {
			File dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Document Dictionary.ser"));
			oos.writeObject(this.documentDictionary);
			oos.flush();
			oos = new ObjectOutputStream(new FileOutputStream(
					dir.getAbsolutePath() + File.separator + "Snippet.ser"));
			oos.writeObject(this.snippet);
			oos.close();
			// System.out.println(this.documentDictionary);
			this.documentDictionary.clear();
			this.snippet.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToCategoryDictionary(TokenStream categoryStream) {
		try {
			while (categoryStream.hasNext()) {
				String text = categoryStream.next().toString();
				if (!this.categoryDictionary.containsKey(text))
					this.categoryDictionary.put(text,
							this.categoryDictionary.size() + 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToCategoryDictionary() {
		try {
			File dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Category Dictionary.ser"));
			oos.writeObject(this.categoryDictionary);
			oos.flush();
			oos.close();
			// System.out.println(this.categoryDictionary);
			this.categoryDictionary.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToCategoryIndex(TokenStream categoryStream) {
		try {
			while (categoryStream.hasNext()) {
				int categoryId = this.categoryDictionary.get(categoryStream
						.next().toString());
				if (this.categoryIndex.containsKey(categoryId)) {
					TermData data = this.categoryIndex.get(categoryId);
					List<Integer> tempList = data.getPostingsList();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = data.getTermFrequency();
					if (tempTF.containsKey(this.currentFileId)) {
						int tf = tempTF.get(this.currentFileId) + 1;
						tempTF.put(this.currentFileId, tf);
					} else
						tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.categoryIndex.put(categoryId, data);
				} else {
					TermData data = new TermData();
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = new HashMap<Integer, Integer>();
					tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.categoryIndex.put(categoryId, data);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToCategoryIndex() {
		try {
			File dir = new File(this.indexDir + File.separator + "category");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Category Index.ser"));
			oos.writeObject(this.categoryIndex);
			oos.flush();
			oos.close();
			// System.out.println(this.categoryIndex);
			this.categoryIndex.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToPlaceDictionary(TokenStream placeStream) {
		try {
			while (placeStream.hasNext()) {
				String text = placeStream.next().toString();
				if (!this.placeDictionary.containsKey(text))
					this.placeDictionary.put(text,
							this.placeDictionary.size() + 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToPlaceDictionary() {
		try {
			File dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Place Dictionary.ser"));
			oos.writeObject(this.placeDictionary);
			oos.flush();
			oos.close();
			// System.out.println(this.placeDictionary);
			this.placeDictionary.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToPlaceIndex(TokenStream placeStream) {
		try {
			while (placeStream.hasNext()) {
				int placeId = this.placeDictionary.get(placeStream.next()
						.toString());
				TermData data;
				if (this.placeIndex.containsKey(placeId)) {
					data = this.placeIndex.get(placeId);
					List<Integer> tempList = data.getPostingsList();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = data.getTermFrequency();
					if (tempTF.containsKey(this.currentFileId)) {
						int tf = tempTF.get(this.currentFileId) + 1;
						tempTF.put(this.currentFileId, tf);
					} else
						tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.placeIndex.put(placeId, data);
				} else {
					data = new TermData();
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = new HashMap<Integer, Integer>();
					tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.placeIndex.put(placeId, data);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToPlaceIndex() {
		try {
			File dir = new File(this.indexDir + File.separator + "place");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Place Index.ser"));
			oos.writeObject(this.placeIndex);
			oos.flush();
			oos.close();
			// System.out.println(this.placeIndex);
			this.placeIndex.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToAuthorDictionary(TokenStream authorStream) {
		try {
			while (authorStream.hasNext()) {
				Token token = authorStream.next();
				String text = token.toString();
				if (!this.authorDictionary.containsValue(text)) {
					this.authorDictionary.put(text, this.currentAuthorOrg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToAuthorDictionary() {
		try {
			File dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Author Dictionary.ser"));
			oos.writeObject(this.authorDictionary);
			oos.flush();
			oos.close();
			// System.out.println(this.authorDictionary);
			this.authorDictionary.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToAuthorIndex(TokenStream authorStream) {
		try {
			while (authorStream.hasNext()) {
				Token token = authorStream.next();
				String authorName = token.toString();
				if (this.authorIndex.containsKey(authorName)) {
					List<Integer> tempList = this.authorIndex.get(authorName);
					tempList.add(this.currentFileId);
					this.authorIndex.put(authorName, tempList);
				} else {
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(this.currentFileId);
					this.authorIndex.put(authorName, tempList);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToAuthorIndex() {
		try {
			File dir = new File(this.indexDir + File.separator + "author");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Author Index.ser"));
			oos.writeObject(this.authorIndex);
			oos.flush();
			oos.close();
			// System.out.println(this.authorIndex);
			this.authorIndex.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToTermDictionary(TokenStream termStream) {
		try {
			while (termStream.hasNext()) {
				String text = termStream.next().toString();
				if (!this.termDictionary.containsKey(text))
					this.termDictionary.put(text,
							this.termDictionary.size() + 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToTermDictionary() {
		try {
			File dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Term Dictionary.ser"));
			oos.writeObject(this.termDictionary);
			oos.flush();
			oos.close();
			// System.out.println(this.termDictionary);
			this.termDictionary.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToTermIndex(TokenStream termStream) {
		try {
			while (termStream.hasNext()) {
				int termId = this.termDictionary.get(termStream.next()
						.toString());
				if (this.termIndex.containsKey(termId)) {
					TermData data = this.termIndex.get(termId);
					List<Integer> tempList = data.getPostingsList();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = data.getTermFrequency();
					if (tempTF.containsKey(this.currentFileId)) {
						int tf = tempTF.get(this.currentFileId) + 1;
						tempTF.put(this.currentFileId, tf);
					} else
						tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.termIndex.put(termId, data);
				} else {
					TermData data = new TermData();
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(this.currentFileId);
					data.setPostingsList(tempList);
					Map<Integer, Integer> tempTF = new HashMap<Integer, Integer>();
					tempTF.put(this.currentFileId, 1);
					data.setTermFrequency(tempTF);
					this.termIndex.put(termId, data);
				}

				if (this.invertedIndex.containsKey(this.currentFileId)) {
					TermData data = this.invertedIndex.get(this.currentFileId);
					Map<Integer, Integer> tempTF = data.getTermFrequency();
					if (tempTF.containsKey(termId)) {
						int count = tempTF.get(termId);
						tempTF.put(termId, count + 1);
					} else {
						tempTF.put(termId, 1);
					}
					data.setTermFrequency(tempTF);
					this.invertedIndex.put(this.currentFileId, data);
				} else {
					TermData data = new TermData();
					Map<Integer, Integer> tempTF = new HashMap<Integer, Integer>();
					tempTF.put(termId, 1);
					data.setTermFrequency(tempTF);
					this.invertedIndex.put(this.currentFileId, data);
				}
			}

			if (this.currentFileId % 100 == 0) {
				writeToTermIndex(false);
				writeToInvertedIndex(false);
				this.count++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToTermIndex(boolean isFinal) {
		try {
			File dir = new File(this.indexDir + File.separator + "term");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos;

			if (isFinal)
				oos = new ObjectOutputStream(new FileOutputStream(
						dir.getAbsolutePath() + File.separator
								+ "Term Index.ser"));
			else
				oos = new ObjectOutputStream(new FileOutputStream(
						dir.getAbsolutePath() + File.separator + "Term Index"
								+ this.count + ".ser"));
			oos.writeObject(this.termIndex);
			oos.flush();
			oos.close();
			// System.out.println("Term index"+count+": "+this.termIndex);
			this.termIndex.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeToInvertedIndex(boolean isFinal) {
		try {
			File dir = new File(this.indexDir + File.separator + "document");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos;
			if (isFinal)
				oos = new ObjectOutputStream(new FileOutputStream(
						dir.getAbsolutePath() + File.separator
								+ "Inverted Index.ser"));
			else
				oos = new ObjectOutputStream(new FileOutputStream(
						dir.getAbsolutePath() + File.separator
								+ "Inverted Index" + this.count + ".ser"));

			oos.writeObject(this.invertedIndex);
			oos.flush();
			oos.close();
			// System.out.println("Inverted index"+count+": "+this.invertedIndex);
			this.invertedIndex.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that indicates that all open resources must be closed and cleaned
	 * and that the entire indexing operation has been completed.
	 * 
	 * @throws IndexerException
	 *             : In case any error occurs
	 */
	public void close() throws IndexerException {
		// TODO
		try {
			writeToCategoryIndex();
			writeToPlaceIndex();
			writeToAuthorIndex();
			writeToTermIndex(false);
			writeToInvertedIndex(false);
			writeToDocumentDictionary();
			writeToCategoryDictionary();
			writeToPlaceDictionary();
			writeToAuthorDictionary();
			writeToTermDictionary();
			merge();
			mergeInvertedIndex();
			calculate();

			this.documentDictionary = null;
			this.snippet = null;
			this.termDictionary = null;
			this.termIndex = null;
			this.categoryDictionary = null;
			this.categoryIndex = null;
			this.placeDictionary = null;
			this.placeIndex = null;
			this.authorDictionary = null;
			this.authorIndex = null;
			this.invertedIndex = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void merge() {
		// recursively merge
		try {
			File ipDirectory = new File(this.indexDir + File.separator + "term");
			String[] files = ipDirectory.list();
			Map<Integer, TermData> mergedMap = new TreeMap<Integer, TermData>();
			ObjectInputStream ois;

			for (String file : files) {
				if (file.equals("Term Index.ser"))
					continue;
				else {
					ois = new ObjectInputStream(new FileInputStream(
							ipDirectory.getAbsolutePath() + File.separator
									+ file));
					mergedMap = mergeFiles(mergedMap,
							(TreeMap<Integer, TermData>) ois.readObject());
					ois.close();
					// System.out.println(mergedMap.size());
				}
			}

			File dir = new File(this.indexDir + File.separator + "term");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Term Index.ser"));
			oos.writeObject(mergedMap);
			oos.flush();
			oos.close();

			// System.out.println("Merge:"+mergedMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void mergeInvertedIndex() {
		// recursively merge
		try {
			File ipDirectory = new File(this.indexDir + File.separator
					+ "document");
			String[] files = ipDirectory.list();
			Map<Integer, TermData> mergedMap = new TreeMap<Integer, TermData>();
			ObjectInputStream ois;

			for (String file : files) {
				if (file.equals("Inverted Index.ser"))
					continue;
				else {
					ois = new ObjectInputStream(new FileInputStream(
							ipDirectory.getAbsolutePath() + File.separator
									+ file));
					mergedMap = mergeFiles(mergedMap,
							(TreeMap<Integer, TermData>) ois.readObject());
					ois.close();
					// System.out.println(mergedMap.size());
				}
			}

			File dir = new File(this.indexDir + File.separator + "document");
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Inverted Index.ser"));
			oos.writeObject(mergedMap);
			oos.flush();
			oos.close();

			// System.out.println("Merge:"+mergedMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public Map<Integer, TermData> mergeFiles(Map<Integer, TermData> m1,
	 * Map<Integer, TermData> m2) { Map<Integer, TermData> mergedMap = new
	 * TreeMap<Integer, TermData>(); Iterator<Entry<Integer, TermData>> i1 =
	 * m1.entrySet().iterator(); while(i1.hasNext()){ Entry<Integer, TermData>
	 * entry = i1.next(); mergedMap.put(entry.getKey(), entry.getValue()); }
	 * 
	 * Iterator<Entry<Integer, TermData>> i2 = m2.entrySet().iterator();
	 * while(i2.hasNext()){ Entry<Integer, TermData> entry = i2.next();
	 * mergedMap.put(entry.getKey(), entry.getValue()); }
	 * 
	 * return mergedMap; }
	 */

	public Map<Integer, TermData> mergeFiles(Map<Integer, TermData> m1,
			Map<Integer, TermData> m2) {
		Map<Integer, TermData> mergedMap = new TreeMap<Integer, TermData>();
		Iterator<Entry<Integer, TermData>> i1 = m1.entrySet().iterator();
		while (i1.hasNext()) {
			Entry<Integer, TermData> entry = i1.next();
			if (mergedMap.containsKey(entry.getKey())) {
				TermData term1 = entry.getValue();
				Map<Integer, Integer> tf1 = term1.getTermFrequency();
				Map<Integer, Integer> tf2 = mergedMap.get(entry.getKey())
						.getTermFrequency();

				Iterator<Entry<Integer, Integer>> tfIterator = tf2.entrySet()
						.iterator();

				while (tfIterator.hasNext()) {
					Entry<Integer, Integer> tempTF = tfIterator.next();
					int key = tempTF.getKey();
					int value = tempTF.getValue();
					if (tf1.containsKey(key)) {
						int newTF = value + tf1.get(key);
						tf1.put(key, newTF);
					} else {
						tf1.put(key, value);
					}
				}

				List<Integer> list1 = term1.getPostingsList();
				List<Integer> list2 = mergedMap.get(entry.getKey())
						.getPostingsList();

				for (Integer docId : list2) {
					if (!list1.contains(docId))
						list1.add(docId);
				}

				term1.setPostingsList(list1);
				term1.setTermFrequency(tf1);
				mergedMap.put(entry.getKey(), term1);
			} else {
				mergedMap.put(entry.getKey(), entry.getValue());
			}
		}

		Iterator<Entry<Integer, TermData>> i2 = m2.entrySet().iterator();
		while (i2.hasNext()) {
			Entry<Integer, TermData> entry = i2.next();
			if (mergedMap.containsKey(entry.getKey())) {
				TermData term1 = entry.getValue();
				Map<Integer, Integer> tf1 = term1.getTermFrequency();
				Map<Integer, Integer> tf2 = mergedMap.get(entry.getKey())
						.getTermFrequency();

				Iterator<Entry<Integer, Integer>> tfIterator = tf2.entrySet()
						.iterator();

				while (tfIterator.hasNext()) {
					Entry<Integer, Integer> tempTF = tfIterator.next();
					int key = tempTF.getKey();
					int value = tempTF.getValue();
					if (tf1.containsKey(key)) {
						int newTF = value + tf1.get(key);
						tf1.put(key, newTF);
					} else {
						tf1.put(key, value);
					}
				}

				List<Integer> list1 = term1.getPostingsList();
				List<Integer> list2 = mergedMap.get(entry.getKey())
						.getPostingsList();

				for (Integer docId : list2) {
					if (!list1.contains(docId))
						list1.add(docId);
				}

				term1.setPostingsList(list1);

				term1.setTermFrequency(tf1);
				mergedMap.put(entry.getKey(), term1);
			} else {
				mergedMap.put(entry.getKey(), entry.getValue());
			}
		}

		return mergedMap;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	public void calculate() {
		try {
			File dir = new File(this.indexDir + File.separator + "document");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					dir.getAbsolutePath() + File.separator
							+ "Inverted Index.ser"));
			this.invertedIndex = (TreeMap<Integer, TermData>) ois.readObject();
			Iterator<Entry<Integer, TermData>> iterator = this.invertedIndex
					.entrySet().iterator();

			this.N = this.invertedIndex.size();
			int length = 0;

			while (iterator.hasNext()) {
				Entry<Integer, TermData> doc = iterator.next();
				TermData docData = doc.getValue();
				int noOfTerms = docData.getTermFrequency().size();
				length += noOfTerms;
				docData.setDocumentLength(noOfTerms);
				this.invertedIndex.put(doc.getKey(), docData);
			}

			this.avgDocLen = (double) length / N;

			writeToInvertedIndex(true);

			dir = new File(this.indexDir + File.separator + "term");
			ois = new ObjectInputStream(new FileInputStream(
					dir.getAbsolutePath() + File.separator + "Term Index.ser"));
			this.termIndex = (TreeMap<Integer, TermData>) ois.readObject();
			iterator = this.termIndex.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<Integer, TermData> term = iterator.next();
				TermData termData = term.getValue();
				int docFreq = termData.getTermFrequency().size();
				double idf = Math.log((double) this.N / docFreq);
				termData.setIdf(idf);
				this.termIndex.put(term.getKey(), termData);
			}

			writeToTermIndex(true);

			ois.close();

			dir = new File(this.indexDir);
			if (!dir.exists())
				dir.mkdir();

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ "Info.ser"));
			String info = this.N + " " + this.avgDocLen;
			oos.writeObject(info);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
