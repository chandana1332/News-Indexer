package edu.buffalo.cse.irf14.analysis;

public class SymbolTokenFilter extends TokenFilter {

	public SymbolTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		if (this.tstream.hasNext()) {
			filter(this.tstream.next());
			return true;
		}
		return false;
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return this.tstream;
	}

	public void filter(Token t) {
		// Where i add the filter part
		String text = t.getTermText();

		// Punctuation
		text = text.replaceAll("[.] ", "");
		text = text.replaceAll("! ", "");
		text = text.replaceAll("[.?!]+$", "");

		// basic rules
		text = text.replaceAll("'s", "");
		// contractions
		text = text.replaceAll("shan't$", "shall not");
		text = text.replaceAll("won't$", "will not");
		text = text.replaceAll("'ve$", " have");
		text = text.replaceAll("n't$", " not");
		text = text.replaceAll("'ll$", " will");
		text = text.replaceAll("'re$", " are");
		text = text.replaceAll("'d$", " would");
		text = text.replaceAll("'m$", " am");
		text = text.replaceAll("'em$", "them");
		text = text.replaceAll("shan't$", "shall not");
		text = text.replaceAll("won't", "will not");
		text = text.replaceAll("'", "");

		// Hyphens

		if (text.matches("[a-zA-Z]+-[a-zA-Z]+"))
			text = text.replaceAll("-", " ");
		else if (!text
				.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]+[a-zA-Z]*[-][a-zA-Z]*[0-9]+[a-zA-Z]*[^a-zA-Z0-9]*")
				&& !text.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]*[a-zA-Z]*[-][a-zA-Z]*[0-9]+[a-zA-Z]*[^a-zA-Z0-9]*")
				&& !text.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]+[a-zA-Z]*[-][a-zA-Z]*[0-9]*[a-zA-Z]+[^a-zA-Z0-9]*"))
			text = text.replaceAll("-+", "");

		t.setTermText(text);

	}

}
