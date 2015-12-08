package edu.buffalo.cse.irf14.analysis;

public class DateTokenFilter extends TokenFilter {

	public DateTokenFilter(TokenStream stream) {
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

		if (text.matches("[0-9]+AD[,.]?|[0-9]+BC[,.]?")) {
			String copy = text;
			text = text.replaceAll("AD|BC", "");
			// System.out.println(text);
			if (text.contains(","))
				t.isComma = true;
			if (text.contains("."))
				t.isDot = true;
			text = text.replaceAll(",", "");
			text = text.replaceAll("[.]", "");
			// System.out.println(text);
			if (text.length() == 1)
				text = "000" + text + "0101";
			if (text.length() == 2)
				text = "00" + text + "0101";
			if (text.length() == 3)
				text = "0" + text + "0101";
			if (text.length() == 4)
				text = text + "0101";
			if (copy.contains("BC"))
				text = "-" + text;
			if (t.isComma)
				text = text + ",";
			else if (t.isDot)
				text = text + ".";
			t.setTermText(text);
			// System.out.println("final"+text);
			// System.out.println(text);

		}
		if (isNumber(text)) {

			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextTerm = t1.getTermText();
				if (BCorAD(nextTerm)) {
					if (text.contains(","))
						t.isComma = true;
					if (text.contains("."))
						t.isDot = true;
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					text = text.replaceAll(",", "");
					if (text.length() == 1)
						text = "000" + text + "0101";
					if (text.length() == 2)
						text = "00" + text + "0101";
					if (text.length() == 3)
						text = "0" + text + "0101";
					if (text.length() == 4)
						text = text + "0101";
					if (nextTerm.equalsIgnoreCase("BC"))
						text = "-" + text;
					if (t1.isComma)
						text = text + ",";
					else if (t1.isDot)
						text = text + ".";
					t.setTermText(text);
					// System.out.println(text);
					tstream.saveCurrent();
					tstream.remove();
					tstream.setCurrent();

				} else {
					tstream.moveBack();
				}
			}

		}

		if (isDate(text)) {
			if (text.contains(","))
				t.isComma = true;
			if (text.contains("."))
				t.isDot = true;
			text = getDate(text);
			// System.out.println("Date:"+text);
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextTerm = t1.getTermText();
				nextTerm = nextTerm.toLowerCase();
				// System.out.println(nextTerm);
				if (isMonth(nextTerm))

				{
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					// System.out.println(nextTerm);
					nextTerm = getMonth(nextTerm);
					// System.out.println("Month:"+nextTerm);

					if (tstream.hasNext()) {

						Token t2 = tstream.next();
						String nextTerm1 = t2.getTermText();

						if (isYear(nextTerm1)) {
							if (nextTerm1.contains(","))
								t2.isComma = true;
							if (nextTerm1.contains("."))
								t2.isDot = true;
							nextTerm1 = nextTerm1.replaceAll("[,]", "");
							// System.out.println("Year:"+nextTerm1);
							// int year=Integer.parseInt(nextTerm1);
							if (t2.isComma)
								text = nextTerm1 + nextTerm + text + ",";
							else if (t2.isDot)
								text = nextTerm1 + nextTerm + text + ".";
							else
								text = nextTerm1 + nextTerm + text;
							t.setTermText(text);
							// System.out.println("final:"+text);
							tstream.saveCurrent();
							tstream.remove();
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						} else {
							tstream.saveCurrent();
							if (t1.isComma)
								text = "1900" + nextTerm + text + ",";
							else if (t1.isDot)
								text = "1900" + nextTerm + text + ".";
							else
								text = "1900" + nextTerm + text;
							// System.out.println("final:"+text);
							t.setTermText(text);
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						}
					}
				} else if (isYear(nextTerm)) {
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					nextTerm = nextTerm.replaceAll("[,]", "");
					nextTerm = nextTerm.replaceAll("[.]", "");
					// System.out.println("Year:"+nextTerm);
					if (tstream.hasNext()) {
						tstream.saveCurrent();
						Token t2 = tstream.next();
						String nextTerm1 = t2.getTermText();
						if (isMonth(nextTerm1 = nextTerm1.toLowerCase())) {
							nextTerm1 = getMonth(nextTerm1);
							if (nextTerm1.contains(","))
								t2.isComma = true;
							if (nextTerm1.contains("."))
								t2.isDot = true;
							nextTerm1 = nextTerm1.replaceAll("[,]", "");
							nextTerm1 = nextTerm1.replaceAll("[.]", "");
							tstream.saveCurrent();
							// System.out.println("Month:"+nextTerm1);
							// int year=Integer.parseInt(nextTerm1);
							if (t2.isComma)
								text = nextTerm + nextTerm1 + text + ",";
							else if (t2.isDot)
								text = nextTerm + nextTerm1 + text + ".";
							else
								text = nextTerm + nextTerm1 + text;
							t.setTermText(text);
							// System.out.println("final:"+text);
							tstream.remove();
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						} else {
							tstream.saveCurrent();
							if (t1.isComma)
								text = nextTerm + "01" + text + ",";
							else if (t1.isDot)
								text = nextTerm + "01" + text + ".";
							else
								text = nextTerm + "01" + text;
							// System.out.println("final:"+text);

							t.setTermText(text);
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						}
					}
				} else {
					tstream.moveBack();
				}
			}
		} else if (isYear(text)) {
			if (text.contains(","))
				t.isComma = true;
			if (text.contains("."))
				t.isDot = true;
			text = text.replaceAll("[,]", "");
			text = text.replaceAll("[.]", "");
			// System.out.println("Year");
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextTerm = t1.getTermText();
				if (isMonth(nextTerm = nextTerm.toLowerCase())) {
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					nextTerm = getMonth(nextTerm);
					// System.out.println("Month:"+nextTerm);

					if (tstream.hasNext()) {
						tstream.saveCurrent();
						Token t2 = tstream.next();
						String nextTerm1 = t2.getTermText();
						if (isDate(nextTerm1)) {
							if (nextTerm1.contains(","))
								t2.isComma = true;
							if (nextTerm1.contains("."))
								t2.isDot = true;
							nextTerm1 = nextTerm1.replaceAll("[,]", "");
							nextTerm1 = nextTerm1.replaceAll("[.]", "");
							tstream.saveCurrent();
							nextTerm1 = getDate(nextTerm1);
							// System.out.println("Date:"+nextTerm1);
							// int year=Integer.parseInt(nextTerm1);
							if (t2.isComma)
								text = text + nextTerm + nextTerm1 + ",";
							else if (t2.isDot)
								text = text + nextTerm + nextTerm1 + ".";
							else
								text = text + nextTerm + nextTerm1;
							// System.out.println("final:"+text);
							t.setTermText(text);
							tstream.remove();
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						} else {
							tstream.saveCurrent();
							if (t1.isComma)
								text = text + nextTerm + "01" + ",";
							else if (t1.isComma)
								text = text + nextTerm + "01" + ".";
							else
								text = text + nextTerm + "01";
							t.setTermText(text);

							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						}

					}
				} else {
					if (t.isComma)
						text = text + "01" + "01" + ",";
					else if (t.isDot)
						text = text + "01" + "01" + ".";
					else
						text = text + "01" + "01";
					t.setTermText(text);
					// System.out.println("final:"+text);

				}
			}
		} else if (isMonth(text.toLowerCase())) {// hereeee
			text = text.toLowerCase();
			if (text.contains(","))
				t.isComma = true;
			if (text.contains("."))
				t.isDot = true;
			text = getMonth(text);
			// System.out.println("Month:"+text);
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextTerm = t1.getTermText();
				// System.out.println(nextTerm);
				if (isYear(nextTerm)) {
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					nextTerm = nextTerm.replaceAll("[,]", "");
					nextTerm = nextTerm.replaceAll("[.]", "");
					// System.out.println("Year:"+nextTerm);

					if (tstream.hasNext()) {
						tstream.saveCurrent();
						Token t2 = tstream.next();
						String nextTerm1 = t2.getTermText();
						if (isDate(nextTerm1)) {
							if (nextTerm1.contains(","))
								t2.isComma = true;
							if (nextTerm1.contains("."))
								t2.isDot = true;
							nextTerm1 = nextTerm1.replaceAll("[,]", "");
							nextTerm1 = nextTerm1.replaceAll("[.]", "");
							tstream.saveCurrent();
							nextTerm1 = getDate(nextTerm1);
							// System.out.println("Date:"+nextTerm1);
							// int year=Integer.parseInt(nextTerm1);
							if (t2.isComma)
								text = nextTerm + text + nextTerm1 + ",";
							if (t2.isDot)
								text = nextTerm + text + nextTerm1 + ".";
							else
								text = nextTerm + text + nextTerm1;
							t.setTermText(text);
							// System.out.println("final:"+text);

							tstream.remove();
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						} else {
							tstream.saveCurrent();
							if (t1.isComma)
								text = nextTerm + text + "01" + ",";
							else if (t1.isDot)
								text = nextTerm + text + "01" + ".";
							else
								text = nextTerm + text + "01";
							t.setTermText(text);
							// System.out.println("final:"+text);
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						}
					}
				} else if (isDate(nextTerm)) {
					if (nextTerm.contains(","))
						t1.isComma = true;
					if (nextTerm.contains("."))
						t1.isDot = true;
					nextTerm = nextTerm.replaceAll("[,]", "");
					nextTerm = nextTerm.replaceAll("[.]", "");
					nextTerm = getDate(nextTerm);
					// System.out.println("Date:"+nextTerm);
					if (tstream.hasNext()) {
						Token t2 = tstream.next();
						String nextTerm1 = t2.getTermText();
						if (isYear(nextTerm1)) {
							if (nextTerm1.contains(","))
								t2.isComma = true;
							if (nextTerm1.contains("."))
								t2.isDot = true;
							nextTerm1 = nextTerm1.replaceAll("[.]", "");
							nextTerm1 = nextTerm1.replaceAll("[,]", "");
							tstream.saveCurrent();
							// System.out.println("Year:"+nextTerm1);
							if (t2.isComma)
								text = nextTerm1 + text + nextTerm + ",";
							else if (t2.isDot)
								text = nextTerm1 + text + nextTerm + ".";
							else
								text = nextTerm1 + text + nextTerm;
							t.setTermText(text);
							// System.out.println("final:"+text);

							tstream.remove();
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						} else {
							tstream.saveCurrent();
							if (t1.isComma)
								text = "1900" + text + nextTerm + ",";
							else if (t1.isDot)
								text = "1900" + text + nextTerm + ".";
							else
								text = "1900" + text + nextTerm;
							// System.out.println("final:"+text);
							t.setTermText(text);
							tstream.moveBack();
							tstream.remove();
							tstream.setCurrent();
						}
					}
				}
			}
		}
		if (text.matches("[0-9]{4}[-][0-9]{4}[,.]?|[0-9]{4}[-][0-9]{2}[,.]?")) {
			if (text.contains(","))
				t.isComma = true;
			else if (text.contains("."))
				t.isDot = true;
			text = text.replaceAll("[,]", "");
			text = text.replaceAll("[.]", "");
			if (text.matches("[0-9]{4}[-][0-9]{2}")) {
				String year[] = text.split("-");
				String prefix = year[0].substring(0, 2);
				// System.out.println(prefix);
				year[0] = year[0] + "0101";
				year[1] = prefix + year[1] + "0101";
				text = year[0] + "-" + year[1];
				// System.out.println(text);
			} else {
				String year[] = text.split("-");
				year[0] = year[0] + "0101";
				year[1] = year[1] + "0101";
				text = year[0] + "-" + year[1];
				// System.out.println(text);
			}
			if (t.isComma)
				text = text + ",";
			else if (t.isDot)
				text = text + ".";
			t.setTermText(text);

		}
		// System.out.println(text);
		// if(text.matches("[0-9]+[:][0-9]+[AM][,.]?|[0-9]+[:][0-9]+[PM][,.]?|[0-9]+[:][0-9]+[:]?[0-9]*[PM][,.]?|[0-9]+[:][0-9]+[:]?[0-9]*[AM][,.]?"))
		if (text.equalsIgnoreCase("5:15PM.")) {
			text = text.toLowerCase();
			// System.out.println(text);
			// PM
			if (text.contains("pm")) {
				if (text.contains("."))
					t.isDot = true;
				if (text.contains(","))
					t.isComma = true;
				text = text.replaceAll("pm", "");
				// System.out.println(text);
				text = text.replaceAll("[,]", "");
				// System.out.println(text);

				text = text.replaceAll("[.]", "");
				// System.out.println(text);
				String time[] = text.split(":");
				if (time.length == 1) {
					int hour = Integer.parseInt(time[0]);
					if (hour >= 1 && hour <= 12) {
						hour = hour + 12;
						time[0] = Integer.toString(hour);
						// System.out.println("Newhour="+time[0]);
					}
					text = time[0] + ":00:00";
				} else if (time.length == 2) {
					// System.out.println(time[0]+" "+time[1]);
					int hour = Integer.parseInt(time[0]);
					if (hour >= 1 && hour <= 12) {
						hour = hour + 12;
						time[0] = Integer.toString(hour);
						// System.out.println("Newhour="+time[0]);
					}
					if (time[1].length() == 1) {
						time[1] = "0" + time[1];
					}
					if (time.length == 2) {
						text = time[0] + ":" + time[1] + ":00";
						// System.out.println(text);
					}
				} else if (time.length == 3) {
					if (time[2].length() == 1)
						time[2] = "0" + time[2];
					text = time[0] + ":" + time[1] + ":" + time[2];
				}

				// System.out.println(text);
				if (t.isComma)
					text = text + ",";
				else if (t.isDot)
					text = text + ".";
				// System.out.println(text);
				t.setTermText(text);
			} else if (text.contains("am")) {
				if (text.contains("."))
					t.isDot = true;
				if (text.contains(","))
					t.isComma = true;
				text = text.replaceAll("am", "");
				// System.out.println(text);
				text = text.replaceAll("[,]", "");
				// System.out.println(text);

				text = text.replaceAll("[.]", "");
				// System.out.println(text);
				String time[] = text.split(":");
				if (time.length == 1) {
					if (time[0].length() == 1)
						time[0] = "0" + time[0];
					text = time[0] + ":00:00";
				}
				if (time.length == 2) {
					// System.out.println(time[0]+" "+time[1]);
					if (time[0].length() == 1)
						time[0] = "0" + time[0];
					if (time[1].length() == 1)
						time[1] = "0" + time[1];
					if (time.length == 2) {
						text = time[0] + ":" + time[1] + ":00";
						// System.out.println(text);
					}
				} else if (time.length == 3) {
					if (time[2].length() == 1)
						time[2] = "0" + time[2];
					text = time[0] + ":" + time[1] + ":" + time[2];
				}

				// System.out.println(text);
				if (t.isComma)
					text = text + ",";
				else if (t.isDot)
					text = text + ".";
				// System.out.println(text);
				t.setTermText(text);
			}
		}

		if (text.matches("[0-9]+[:]?[0-9]*[:]?[0-9]*")) {
			if (tstream.hasNext()) {
				Token t1 = tstream.next();
				String nextTerm = t1.getTermText();
				nextTerm = nextTerm.toLowerCase();
				if (nextTerm.matches("[am]?[pm]?[.,]?")) {
					if (nextTerm.contains("pm")) {
						if (nextTerm.contains("."))
							t1.isDot = true;
						if (nextTerm.contains(","))
							t1.isComma = true;
						// System.out.println(text);
						text = text.replaceAll("[,]", "");
						nextTerm = nextTerm.replaceAll("[,]", "");
						// System.out.println(text);

						text = text.replaceAll("[.]", "");
						nextTerm = nextTerm.replaceAll("[.]", "");
						// System.out.println(text);
						String time[] = text.split(":");
						if (time.length == 1) {
							int hour = Integer.parseInt(time[0]);
							if (hour >= 1 && hour <= 12) {
								hour = hour + 12;
								time[0] = Integer.toString(hour);
								// System.out.println("Newhour="+time[0]);
							}
							text = time[0] + ":00:00";
						} else if (time.length == 2) {
							// System.out.println(time[0]+" "+time[1]);
							int hour = Integer.parseInt(time[0]);
							if (hour >= 1 && hour <= 12) {
								hour = hour + 12;
								time[0] = Integer.toString(hour);
								// System.out.println("Newhour="+time[0]);
							}
							if (time[1].length() == 1) {
								time[1] = "0" + time[1];
							}
							if (time.length == 2) {
								text = time[0] + ":" + time[1] + ":00";
								// System.out.println(text);
							}
						} else if (time.length == 3) {
							if (time[2].length() == 1)
								time[2] = "0" + time[2];
							text = time[0] + ":" + time[1] + ":" + time[2];
						}
						// System.out.println(text);
						if (t1.isComma)
							text = text + ",";
						else if (t1.isDot)
							text = text + ".";
						// System.out.println(text);
						tstream.saveCurrent();
						// System.out.println("final:"+text);
						t.setTermText(text);

						tstream.remove();
						tstream.setCurrent();

					} else if (nextTerm.contains("am")) {
						if (nextTerm.contains("."))
							t1.isDot = true;
						if (nextTerm.contains(","))
							t1.isComma = true;
						text = text.replaceAll(",", "");
						// System.out.println(text);
						nextTerm = nextTerm.replaceAll("[,]", "");
						// System.out.println(text);
						nextTerm = nextTerm.replaceAll("[.]", "");
						text = text.replaceAll("[.]", "");
						// System.out.println(text);
						String time[] = text.split(":");
						if (time.length == 1) {
							if (time[0].length() == 1)
								time[0] = "0" + time[0];
							text = time[0] + ":00:00";
						}
						if (time.length == 2) {
							// System.out.println(time[0]+" "+time[1]);
							if (time[0].length() == 1)
								time[0] = "0" + time[0];
							if (time[1].length() == 1)
								time[1] = "0" + time[1];
							if (time.length == 2) {
								text = time[0] + ":" + time[1] + ":00";
								// System.out.println(text);
							}
						} else if (time.length == 3) {
							if (time[2].length() == 1)
								time[2] = "0" + time[2];
							text = time[0] + ":" + time[1] + ":" + time[2];
						}
						// System.out.println(text);
						if (t1.isComma)
							text = text + ",";
						else if (t1.isDot)
							text = text + ".";
						// System.out.println(text);
						tstream.saveCurrent();
						// System.out.println("final:"+text);
						t.setTermText(text);
						tstream.remove();
						tstream.setCurrent();
					}

				} else {
					tstream.moveBack();
				}
			}
		}
	}

	//
	// //System.out.println(text);
	// t.setTermText(text);
	// }
	//

	public boolean BCorAD(String s) {
		if (s.matches("BC[,.]?|AD[,.]?")) {
			return true;
		}
		return false;
	}

	public boolean isNumber(String s) {

		if (s.matches("[0-9]+[,.]|[0-9]+|[,.][0-9]+")) {
			// System.out.println("Number");
			s = s.replaceAll("[,]", "");
			s = s.replaceAll("[.]", "");
			return true;
		}
		return false;

	}

	public boolean isDate(String s) {

		if (s.matches("[0-9]+[,.]|[0-9]+|[.,][0-9]+")) {
			// System.out.println("Number");
			s = s.replaceAll("[,]", "");
			s = s.replaceAll("[.]", "");
			int date = Integer.parseInt(s);
			if (date >= 1 && date <= 31) {

				return true;
			}
		}
		return false;
	}

	public String getDate(String s) {
		s = s.replaceAll("[,]", "");
		s = s.replaceAll("[.]", "");
		int date = Integer.parseInt(s);
		if (date >= 1 && date <= 9)
			s = "0" + s;
		// System.out.println(s);
		return s;
	}

	public boolean isMonth(String s) {

		if (s.matches("jan[,.]?|january[,.]?|feb[.,]?|february[,.]?|mar[.,]?|march[,.]?|apr[.,]?|april[,.]?|may[,.]?|june[,.]?|^jun[.,]?|jul[.,]?|july[,.]?|aug[.,]?|august[,.]?|sep[.,]?|september[,.]?|oct[.,]?|october[,.]?|nov[.,]?|november[,.]?|dec[.,]?|december[,.]?")) {
			return true;
		}
		return false;
	}

	public String getMonth(String s) {
		String mon = null;
		if (s.matches("jan[.,]?|january[.,]?"))
			mon = "01";
		else if (s.matches("feb[.,]?|february[.,]?"))
			mon = "02";
		else if (s.matches("mar[.,]?|march[.,]?"))
			mon = "03";
		else if (s.matches("apr[.,]?|april[.,]?"))
			mon = "04";
		else if (s.matches("may[.,]?"))
			mon = "05";
		else if (s.matches("jun[.,]?|june[.,]?"))
			mon = "06";
		else if (s.matches("jul[.,]?|july[.,]?"))
			mon = "07";
		else if (s.matches("aug[.,]?|august[.,]?"))
			mon = "08";
		else if (s.matches("sep[.,]?|september[.,]?"))
			mon = "09";
		else if (s.matches("oct[.,]?|october[.,]?"))
			mon = "10";
		else if (s.matches("nov[.,]?|november[.,]?"))
			mon = "11";
		else if (s.matches("dec[.,]?|december[.,]?"))
			mon = "12";

		return mon;
	}

	public boolean isYear(String s) {
		if (s.matches("[0-9]+[,.]|[0-9]+|[,.][0-9]+")) {
			s = s.replaceAll("[,]", "");
			s = s.replaceAll("[.]", "");
			if (s.length() == 4) {
				return true;
			}

		}
		return false;

	}
}
