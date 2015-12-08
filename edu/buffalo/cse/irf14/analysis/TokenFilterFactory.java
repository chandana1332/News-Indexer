package edu.buffalo.cse.irf14.analysis;

/**
 * Factory class for instantiating a given TokenFilter
 * 
 * @author chandana
 *
 */
public class TokenFilterFactory {
	/**
	 * Static method to return an instance of the factory class. Usually factory
	 * classes are defined as singletons, i.e. only one instance of the class
	 * exists at any instance. This is usually achieved by defining a private
	 * static instance that is initialized by the "private" constructor. On the
	 * method being called, you return the static instance. This allows you to
	 * reuse expensive objects that you may create during instantiation
	 * 
	 * @return An instance of the factory
	 */
	public static TokenFilterFactory getInstance() {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		return new TokenFilterFactory();
	}

	/**
	 * Returns a fully constructed {@link TokenFilter} instance for a given
	 * {@link TokenFilterType} type
	 * 
	 * @param type
	 *            : The {@link TokenFilterType} for which the
	 *            {@link TokenFilter} is requested
	 * @param stream
	 *            : The TokenStream instance to be wrapped
	 * @return The built {@link TokenFilter} instance
	 */
	public TokenFilter getFilterByType(TokenFilterType type, TokenStream stream) {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		switch (type) {
		case SYMBOL:
			return new SymbolTokenFilter(stream);
		case DATE:
			return new DateTokenFilter(stream);
		case NUMERIC:
			return new NumericTokenFilter(stream);
		case CAPITALIZATION:
			return new CapitalizationTokenFilter(stream);
		case STOPWORD:
			return new StopWordTokenFilter(stream);
		case STEMMER:
			return new StemmerTokenFilter(stream);
		case ACCENT:
			return new AccentTokenFilter(stream);
		case SPECIALCHARS:
			return new SpecialCharTokenFilter(stream);
		default:
			return null;
		}
	}
}
