package edu.buffalo.cse.irf14.analysis;

public class NumericTokenFilter extends TokenFilter {

	public NumericTokenFilter(TokenStream stream) {
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

		if (text.matches("[0-9]{2}[:][0-9]{2}[:][0-9]{2}")) {
		} else if (text.matches("[0-9]+") || text.matches("[0-9]+,[0-9]+")
				|| text.matches("[0-9]+[.][0-9]+")) {
			int len = text.length();
			if (len != 8) {
				tstream.remove();
				// System.out.println("Term Removed");

			} else {

				String mm = text.substring(4, 6);
				String dd = text.substring(6, 8);
				// System.out.println(mm+":"+dd);
				int m = Integer.parseInt(mm);
				int d = Integer.parseInt(dd);
				if (m >= 1 && m <= 12) {
					if (d >= 1 && d <= 31) {

					} else {

						tstream.remove();
						// System.out.println("Term Removed");

					}
				} else {
					tstream.remove();
					// System.out.println("Term Removed");

				}
			}

		} else {
			text = text.replaceAll("[0-9]+,[0-9]+", "");
			text = text.replaceAll("[0-9]+[.][0-9]+", "");
			text = text.replaceAll("[0-9]", "");
		}
		// System.out.println(text);
		t.setTermText(text);
	}

}
