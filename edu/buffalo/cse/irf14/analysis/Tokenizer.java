package edu.buffalo.cse.irf14.analysis;

/**
 * @author chandana
 *  Class that converts a given string into a
 *         {@link TokenStream} instance
 */
public class Tokenizer {
	/**
	 * Default constructor. Assumes tokens are whitespace delimited
	 */
	private String delimiter;

	public Tokenizer() {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		this.delimiter = " ";
	}

	/**
	 * Overloaded constructor. Creates the tokenizer with the given delimiter
	 * 
	 * @param delim
	 *            : The delimiter to be used
	 */
	public Tokenizer(String delim) {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		this.delimiter = delim;
	}

	/**
	 * Method to convert the given string into a TokenStream instance. This must
	 * only break it into tokens and initialize the stream. No other processing
	 * must be performed. Also the number of tokens would be determined by the
	 * string and the delimiter. So if the string were "hello world" with a
	 * whitespace delimited tokenizer, you would get two tokens in the stream.
	 * But for the same text used with lets say "~" as a delimiter would return
	 * just one token in the stream.
	 * 
	 * @param str
	 *            : The string to be consumed
	 * @return : The converted TokenStream as defined above
	 * @throws TokenizerException
	 *             : In case any exception occurs during tokenization
	 */
	public TokenStream consume(String str) throws TokenizerException {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		if (str == null || str.equals(""))
			throw new TokenizerException();

		Token token;
		str = str.toLowerCase();

		String splitArray[] = str.split(delimiter);
		TokenStream tokenStream = new TokenStream();

		for (String splitText : splitArray) {
			token = new Token();
			token.setTermText(splitText);
			tokenStream.add(token);

		}

		return tokenStream;
	}
}
