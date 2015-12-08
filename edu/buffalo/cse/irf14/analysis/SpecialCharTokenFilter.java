package edu.buffalo.cse.irf14.analysis;

public class SpecialCharTokenFilter extends TokenFilter {

	public SpecialCharTokenFilter(TokenStream stream) {
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
		String text = t.getTermText();
		if (text.matches("[a-zA-Z]+-[a-zA-Z]+")) {
			text = text.replaceAll("-", " ");

		} else if (!text
				.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]+[a-zA-Z]*[-][a-zA-Z]*[0-9]+[a-zA-Z]*[^a-zA-Z0-9]*")
				&& !text.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]*[a-zA-Z]*[-][a-zA-Z]*[0-9]+[a-zA-Z]*[^a-zA-Z0-9]*")
				&& !text.matches("[^a-zA-Z0-9]*[a-zA-Z]*[0-9]+[a-zA-Z]*[-][a-zA-Z]*[0-9]*[a-zA-Z]+[^a-zA-Z0-9]*"))
			text = text.replaceAll("-+", "");

		text = text.replaceAll("[^a-zA-Z0-9\\.\\s\\?!'-]", "");

		t.setTermText(text);
	}

}
