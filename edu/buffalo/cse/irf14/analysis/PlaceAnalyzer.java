package edu.buffalo.cse.irf14.analysis;

public class PlaceAnalyzer implements Analyzer {

	TokenStream tstream;

	public PlaceAnalyzer(TokenStream tstream) {
		// TODO Auto-generated constructor stub
		this.tstream = tstream;
	}

	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		analyze();
		return false;
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return this.tstream;
	}

	public void analyze() {
		try {
			TokenFilterFactory factory = TokenFilterFactory.getInstance();

			this.tstream.reset();

			TokenFilter filter = factory.getFilterByType(
					TokenFilterType.SYMBOL, this.tstream);
			if (filter != null) {
				while (filter.increment()) {

				}
				this.tstream = filter.getStream();
			}
			this.tstream.reset();
			filter = factory.getFilterByType(TokenFilterType.ACCENT,
					this.tstream);
			if (filter != null) {
				while (filter.increment()) {

				}
				this.tstream = filter.getStream();
			}
			this.tstream.reset();
			filter = factory.getFilterByType(TokenFilterType.SPECIALCHARS,
					this.tstream);
			if (filter != null) {
				while (filter.increment()) {

				}
				this.tstream = filter.getStream();
			}
			this.tstream.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
