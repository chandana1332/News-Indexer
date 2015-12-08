package edu.buffalo.cse.irf14.analysis;

public class Capitalization1TokenFilter extends TokenFilter {

	public Capitalization1TokenFilter(TokenStream stream) {
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
		while (tstream.hasNext()) {
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextToken = t1.getTermText();

				if (!nextToken.matches("and|And|AND")) {
					t.merge(t1);
					String a = t.getTermText();
					tstream.remove();
				}
			}
		}

	}
}