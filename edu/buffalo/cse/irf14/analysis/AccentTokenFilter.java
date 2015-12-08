package edu.buffalo.cse.irf14.analysis;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class AccentTokenFilter extends TokenFilter {

	public AccentTokenFilter(TokenStream stream) {
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
		// System.out.println(text);
		text = text == null ? null : Normalizer.normalize(text, Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		// System.out.println(text);

		t.setTermText(text);
	}

}
