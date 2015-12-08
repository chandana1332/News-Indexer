package edu.buffalo.cse.irf14.analysis;

public class StopWordTokenFilter extends TokenFilter {

	public StopWordTokenFilter(TokenStream stream) {
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
		// System.out.println(text);
		int i = 0;
		String words[] = { "its", "be", "at", "if", "of", "is", "are", "not",
				"why", "who", "where", "will", "us", "me", "you", "I", "am",
				"a", "and", "all", "but", "by", "did", "do", "for", "he",
				"she", "in", "no", "on", "off", "our", "only", "out", "so",
				"the", "this", "to", "up", "was", "we", "were" };
		int len = words.length;

		for (i = 0; i < len; i++)
			if (text.equalsIgnoreCase(words[i])) {
				tstream.remove();
			}

		// System.out.println(text);
		t.setTermText(text);
	}

}
