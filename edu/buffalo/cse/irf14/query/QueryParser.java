package edu.buffalo.cse.irf14.query;

import edu.buffalo.cse.irf14.document.ParserException;

/**
 * @author chandana 
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {
	/**
	 * MEthod to parse the given user query into a Query object
	 * 
	 * @param userQuery
	 *            : The query to parse
	 * @param defaultOperator
	 *            : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator)
			throws ParserException {
		// TODO: YOU MUST IMPLEMENT THIS METHOD
		Query q = new Query();
		String query = "";
		userQuery = userQuery.replace("( ", "(");
		userQuery = userQuery.replace(" )", ")");
		if (userQuery == null || userQuery.equals(""))
			throw new ParserException();

		int count = 0;
		String b = null;
		String tempArray[] = userQuery.split(" ");
		int len = tempArray.length;

		if (userQuery == null || userQuery.equals(""))
			throw new ParserException();

		// HANDLING PHRASES

		for (int i = 0; i < tempArray.length; i++) {
			int j = 0;
			if (tempArray[i].matches(".*[\"].*")) {
				if (tempArray[i].matches(".*[\"].*[\"]"))
					break;
				for (j = i + 1; j < len; j++) {
					if (!tempArray[j].matches(".*[\"].*"))
						tempArray[i] = tempArray[i] + " " + tempArray[j];
					else
						break;

				}
				tempArray[i] = tempArray[i] + " " + tempArray[j];

				len = i + 1;
				// System.out.println(tempArray[i]+"len:"+len);
				if (len + 1 < tempArray.length) {
					for (int c = j + 1; c < tempArray.length; c++) {
						tempArray[len++] = tempArray[c];
					}
				} else
					break;

			}

		}

		// RESOLVE BRACKETS
		for (int i = 0; i < len; i++) {

			if (tempArray[i].toLowerCase().matches(".*category:[(].*")) {
				tempArray[i] = tempArray[i].toLowerCase().replaceAll(
						"category:[(]", "(Category:");

				int j = i + 1;
				if (j < len) {
					while (!tempArray[j].matches(".*[)]")) {
						if (!tempArray[j].matches("AND|NOT|OR"))
							tempArray[j] = "Category:" + tempArray[j];
						j++;
					}
					tempArray[j] = "Category:" + tempArray[j];
				}
			} else if (tempArray[i].toLowerCase().matches(".*place:[(].*")) {
				tempArray[i] = tempArray[i].toLowerCase().replaceAll(
						"place:[(]", "(Place:");

				int j = i + 1;
				while (!tempArray[j].matches(".*[)]")) {
					if (!tempArray[j].matches("AND|NOT|OR"))
						tempArray[j] = "Place:" + tempArray[j];
					j++;
				}
				tempArray[j] = "Place:" + tempArray[j];
			} else if (tempArray[i].toLowerCase().matches(".*author:[(].*")) {
				tempArray[i] = tempArray[i].toLowerCase().replaceAll(
						"author:[(]", "(Author:");

				int j = i + 1;
				while (!tempArray[j].matches(".*[)]")) {
					if (!tempArray[j].matches("AND|NOT|OR"))
						tempArray[j] = "Author:" + tempArray[j];
					j++;
				}
				tempArray[j] = "Author:" + tempArray[j];
			} else if (tempArray[i].matches("[(]+.*")) {
				String a = tempArray[i].replaceAll("[(]+", "");
				if (!a.toLowerCase().matches("category:.*"))
					if (!a.toLowerCase().matches("place:.*"))
						if (!a.toLowerCase().matches("author:.*"))
							if (!a.toLowerCase().matches("term:.*")) {
								b = "Term:" + a;

								tempArray[i] = tempArray[i].replaceAll(a, b);
							}

				int j = i + 1;
				if (j < len) {
					while (!tempArray[j].matches(".*[)]")) {
						if (tempArray[j].matches("[(]+.*"))
							break;
						if (!tempArray[j].matches("AND|NOT|OR"))
							if (!tempArray[j].toLowerCase().matches(
									"category:.*"))
								if (!tempArray[j].toLowerCase().matches(
										"place:.*"))
									if (!tempArray[j].toLowerCase().matches(
											"author:.*"))
										if (!tempArray[j].toLowerCase()
												.matches("term:.*")) {
											tempArray[j] = "Term:"
													+ tempArray[j];
										}

						j++;
					}
					if (!tempArray[j].matches("AND|NOT|OR"))
						if (!tempArray[j].toLowerCase().matches("category:.*"))
							if (!tempArray[j].toLowerCase().matches("place:.*"))
								if (!tempArray[j].toLowerCase().matches(
										"author:.*"))
									if (!tempArray[j].toLowerCase().matches(
											"term:.*"))
										if (!tempArray[j].matches("[(]+.*")) {
											tempArray[j] = "Term:"
													+ tempArray[j];
										}
				}
			} else {
				if (!tempArray[i].matches("AND|OR|NOT"))
					if (!tempArray[i].toLowerCase().contains("term:"))
						if (!tempArray[i].toLowerCase().contains("category:"))
							if (!tempArray[i].toLowerCase().contains("place:"))
								if (!tempArray[i].toLowerCase().contains(
										"author:"))
									tempArray[i] = "Term:" + tempArray[i];
			}

		}

		// ABSORB NOTs

		for (int i = 0; i < len; i++) {
			if (tempArray[i].matches("NOT")) {
				tempArray[i] = "AND";
				if (tempArray[i + 1].matches("[(].*")) {
					tempArray[i + 1] = tempArray[i + 1].replaceAll("[(]", "(<");
					// //System.out.println(tempArray[i+1]);
					int j = i + 2;
					while (!tempArray[j].matches(".*[)]")) {
						j++;
					}
					tempArray[j] = tempArray[j].replaceAll("[)]", ">)");
				} else {
					if (tempArray[i + 1].matches(".*[)]")) {
						tempArray[i + 1] = tempArray[i + 1].replaceAll("[)]",
								">)");
						tempArray[i + 1] = "<" + tempArray[i + 1];
					} else
						tempArray[i + 1] = "<" + tempArray[i + 1] + ">";

				}
			}
		}
		for (int i = 0; i < len; i++) {
			tempArray[i] = tempArray[i].replaceAll("[(]", "[ ");
			tempArray[i] = tempArray[i].replaceAll("[)]", " ]");

		}

		int i = 0;
		while (i < len) {
			if (!tempArray[i].matches("AND|OR"))
				if (i < len - 1)
					if (!tempArray[i + 1].matches("AND|OR")) {
						tempArray[i] = "$" + tempArray[i] + " "
								+ defaultOperator;
						count++;

						int j = i + 1;

						while (j < len - 1) {
							if (tempArray[j].matches("AND|OR")) {
								break;
							} else {
								if (tempArray[j + 1].matches("AND|OR")) {
									break;
								}
								count++;
								tempArray[j] = tempArray[j] + " "
										+ defaultOperator;

								j++;
							}
							i = j;

						}

						tempArray[j] = tempArray[j] + "$";

					}

			i++;

			for (int k = 0; k < len; k++) {
				// System.out.println(tempArray[k]);
				if (tempArray[k].matches("[$].*"))
					if (count < len - 1) {
						if (!tempArray[k].contains("[")) {
							if (!tempArray[k].contains("]")) {
								tempArray[k] = tempArray[k].replaceAll("[$]",
										"[ ");
								for (int j = k + 1; j < len; j++) {
									if (tempArray[j].matches(".*[$]"))
										tempArray[j] = tempArray[j].replaceAll(
												"[$]", " ]");
								}
							} else {
								tempArray[k] = tempArray[k].replaceAll("[$]",
										"");
								for (int j = k + 1; j < len; j++) {
									if (tempArray[j].matches(".*[$]"))
										tempArray[j] = tempArray[j].replaceAll(
												"[$]", "");
								}
							}
						}

						else {
							tempArray[k] = tempArray[k].replaceAll("[$]", "");
							for (int j = k + 1; j < len; j++) {
								if (tempArray[j].matches(".*[$]"))
									tempArray[j] = tempArray[j].replaceAll(
											"[$]", "");
							}
						}

					} else if (count == len - 1) {
						tempArray[k] = tempArray[k].replaceAll("[$]", "");
						for (int j = k + 1; j < len; j++) {
							if (tempArray[j].matches(".*[$]"))
								tempArray[j] = tempArray[j].replaceAll("[$]",
										"");
						}
					}
			}

		}

		for (i = 0; i < len; i++) {
			if (tempArray[i].toLowerCase().contains("category:"))
				tempArray[i] = tempArray[i]
						.replaceAll("category:", "Category:");

			if (tempArray[i].toLowerCase().contains("place:"))
				tempArray[i] = tempArray[i].replaceAll("place:", "Place:");

			if (tempArray[i].toLowerCase().contains("author:"))
				tempArray[i] = tempArray[i].replaceAll("author:", "Author:");

			if (tempArray[i].toLowerCase().contains("term:"))
				tempArray[i] = tempArray[i].replaceAll("term:", "Term:");
		}

		query = "";

		for (int w = 0; w < len; w++) {
			query = query + tempArray[w] + " ";
		}

		query = "{ " + query + "}";
		// System.out.println("FINAL:"+query);

		q.setQuerystring(query);

		return q;

	}

}
