package edu.buffalo.cse.irf14.analysis;

public class Capitalization2TokenFilter extends TokenFilter {

	public Capitalization2TokenFilter(TokenStream stream) {
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
		while (tstream.hasNext()) {
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextToken = t1.getTermText();

				if (!nextToken.matches(".*[,]")) {
					// System.out.println("T2 camel");
					t.merge(t1);
					String a = t.getTermText();
					// System.out.println(a);
					tstream.remove();
				}
			}
		}

	}
}