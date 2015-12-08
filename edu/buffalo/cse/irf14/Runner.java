package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.index.IndexerException;

/**
 * @author chandana
 *
 */
public class Runner {

	/**
	 * 
	 */
	public Runner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String ipDir = args[0];
			String indexDir = args[1];
			String corpusDir = args[2];

			// more? idk!

			File ipDirectory = new File(ipDir);
			String[] catDirectories = ipDirectory.list();

			String[] files;
			File dir;

			Document d = null;
			IndexWriter writer = new IndexWriter(indexDir);

			for (String cat : catDirectories) {
				dir = new File(ipDir + File.separator + cat);
				files = dir.list();

				if (files == null)
					continue;

				for (String f : files) {
					try {
						// System.out.println(f);
						d = Parser.parse(dir.getAbsolutePath() + File.separator
								+ f);
						writer.addDocument(d);
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}

			writer.close();

			IndexReader reader = new IndexReader(indexDir, IndexType.TERM);
			System.out.println("Key terms: " + reader.getTotalKeyTerms());

			char mode = 'Q';

			SearchRunner runner;

			switch (mode) {
			case 'E':
				FileOutputStream outputStream = new FileOutputStream(corpusDir
						+ File.separator + "output.txt");
				PrintStream stream = new PrintStream(outputStream);
				runner = new SearchRunner(indexDir, corpusDir, mode, stream);
				runner.query(new File(corpusDir + File.separator + "query.txt"));
				break;
			case 'Q':
				stream = System.out;
				runner = new SearchRunner(indexDir, corpusDir, mode, stream);
				while (true) {
					stream.print("Enter query: ");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(System.in));
					String query = br.readLine();
					runner.query(query, ScoringModel.OKAPI);
				}
			default:
				System.out.println("Invalid option");
			}
		} catch (Exception e) {

		}
	}

}
