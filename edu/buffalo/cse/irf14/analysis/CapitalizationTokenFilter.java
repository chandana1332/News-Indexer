package edu.buffalo.cse.irf14.analysis;

public class CapitalizationTokenFilter extends TokenFilter {

	public CapitalizationTokenFilter(TokenStream stream) {
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
		// //System.out.println(text);
		int flag = 0;
		boolean s = false;
		if (isStart()) {
			// //System.out.println("First word");
			if (isUpper(text)) {
				// //System.out.println("It is uppercase");
				tstream.saveCurrent();
				while (!text.matches(".+[.?!]") && !isEnd()) {
					s = checkNextToken();
					if (!s) {
						flag = 1;
						break;
					}
				}
				if (flag == 1) {
					// check for camel case
					tstream.setCurrent();
					// //System.out.println("The whole sentence is not uppercase");
					// text=text.toLowerCase();

					if (isTitle(text)) {
						int x = 0;
						// //System.out.println("T1 camel");
						while (!isEnd() && !text.matches(".+[.?!]")) {
							if (tstream.hasNext()) {
								Token t1 = tstream.next();
								String nextToken = t1.getTermText();

								if (isTitle(nextToken)) {
									x++;
									// //System.out.println("T2 camel");
									t.merge(t1);
									String a = t.getTermText();
									// //System.out.println(a);
									tstream.remove();
								} else {
									tstream.moveBack();
									break;
								}
							}
						}
						if (tstream.hasNext()) {
							Token t1 = tstream.next();
							String nextToken = t1.getTermText();
							if (nextToken.matches(".+[.?!]")) {
								x++;
								// //System.out.println("T2 camel");
								t.merge(t1);
								String a = t.getTermText();
								// //System.out.println(a);
								tstream.remove();
							} else {
								tstream.moveBack();

							}
						}
						if (x == 0) {
							if (isTitle(text)) {
								text = text.toLowerCase();
								t.setTermText(text);
							}
						}
					}

				} else {
					// //System.out.println("Whole sentence is in uppercase");
					tstream.setCurrent();
					text = text.toLowerCase();
					t.setTermText(text);
					do {
						String temp1 = null;
						if (tstream.hasNext()) {
							Token t1 = tstream.next();
							temp1 = t1.getTermText();
							// //System.out.println("next:"+temp1);
							temp1 = temp1.toLowerCase();
							t1.setTermText(temp1);
							// //System.out.println("temp:"+temp1);
						}
					} while (!isEnd() && !text.matches(".+[.?!]"));
					String temp1 = null;
					if (tstream.hasNext()) {
						Token t1 = tstream.next();
						temp1 = t1.getTermText();
						// //System.out.println("next:"+temp1);
						temp1 = temp1.toLowerCase();
						t1.setTermText(temp1);
						// //System.out.println("temp:"+temp1);
						// //System.out.println(text);
						tstream.setCurrent();
					}
				}
			}

			if (isTitle(text)) {
				int x = 0;
				text = text.toLowerCase();
				t.setTermText(text);
				if (isTitle(text)) {
					// //System.out.println("T1 camel");
					while (!isEnd() && !text.matches(".+[.?!]")) {
						if (tstream.hasNext()) {
							Token t1 = tstream.next();
							String nextToken = t1.getTermText();

							if (isTitle(nextToken)) {
								x++;
								// //System.out.println("T2 camel");
								t.merge(t1);
								String a = t.getTermText();
								// //System.out.println(a);
								tstream.remove();
							} else {
								tstream.moveBack();
								break;
							}
						}
					}
					if (tstream.hasNext()) {
						Token t1 = tstream.next();
						String nextToken = t1.getTermText();
						if (nextToken.matches(".+[.?!]")) {
							if (isTitle(nextToken)) {
								x++;
								// //System.out.println("T2 camel");
								t.merge(t1);
								String a = t.getTermText();
								// //System.out.println(a);
								tstream.remove();
							} else {
								tstream.moveBack();

							}
						}
					}
					if (x == 0) {
						text = text.toLowerCase();
						t.setTermText(text);

					}

				}
			}

		} else {
			tstream.saveCurrent();

			// //System.out.println("Not the first word");

			if (isTitle(text)) {
				// //System.out.println("T1 camel");
				while (!isEnd() && !text.matches(".+[.?!]")) {
					if (tstream.hasNext()) {
						Token t1 = tstream.next();
						String nextToken = t1.getTermText();

						if (isTitle(nextToken)) {
							// //System.out.println("T2 camel");
							t.merge(t1);
							String a = t.getTermText();
							// //System.out.println(a);
							tstream.remove();
						} else {
							tstream.moveBack();
							break;
						}
					}
				}
				if (tstream.hasNext()) {
					Token t1 = tstream.next();
					String nextToken = t1.getTermText();
					if (nextToken.matches(".+[.?!]")) {

						// //System.out.println("T2 camel");
						t.merge(t1);
						String a = t.getTermText();
						// //System.out.println(a);
						tstream.remove();
					} else {
						tstream.moveBack();

					}
				}

			}

		}
		// //System.out.println(text);

	}

	public boolean isLower(String a) {
		for (char c : a.toCharArray()) {
			if (Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	public boolean isUpper(String a) {
		for (char c : a.toCharArray()) {
			if (Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

	public boolean isTitle(String a) {
		int i = 0;
		char c[] = a.toCharArray();
		if (c.length > 1) {
			for (i = 0; i < c.length; i++) {
				if (Character.isLetter(c[i])) {
					if (Character.isUpperCase(c[i]) && !isUpper(a)) {
						return true;
					} else
						return false;

				}

			}

		} else {
			if (isUpper(a))
				return true;
		}
		return false;
	}

	public boolean checkNextToken() {
		String text = null;
		if (tstream.hasNext()) {
			Token t1 = tstream.next();
			text = t1.getTermText();
			if (isUpper(text))
				return true;

		}
		tstream.moveBack();
		// //System.out.println("Nextword Not Uppercase");
		return false;
	}

	public boolean checkNextTokenCase() {
		String text = null;
		if (tstream.hasNext()) {
			Token t1 = tstream.next();
			text = t1.getTermText();
			if (!isLower(text) && !isUpper(text))
				return true;

		}
		tstream.moveBack();
		// //System.out.println("Nextword Not Uppercase");
		return false;
	}

	public boolean isEnd() {

		if (!tstream.hasNext())
			return true;
		else {
			String text = null;
			Token t11 = tstream.next();
			text = t11.getTermText();
			if (text.matches(".+[.?!]")) {
				tstream.moveBack();
				return true;

			}
			tstream.moveBack();

		}

		return false;

	}

	public boolean isStart() {

		if (!tstream.hasPrevious())
			return true;
		else {
			tstream.saveCurrent();
			Token t11 = tstream.previous();
			if (t11 != null) {
				String text = null;
				text = t11.getTermText();
				if (text.matches(".+[.?!]")) {
					tstream.setCurrent();
					return true;

				}
			} else {
				Token t12 = tstream.previous();
				if (t12 != null) {
					String text = null;
					text = t12.getTermText();
					if (text.matches(".+[.?!]")) {
						tstream.setCurrent();
						return true;
					}
				} else {
					Token t13 = tstream.previous();
					if (t13 != null) {
						String text = null;
						text = t13.getTermText();
						if (text.matches(".+[.?!]")) {
							tstream.setCurrent();
							return true;
						}
					}

				}

			}
		}
		tstream.setCurrent();
		return false;

	}

}
