package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author chandana
 * 			 Class that represents a stream of Tokens. All
 *         {@link Analyzer} and {@link TokenFilter} instances operate on this to
 *         implement their behavior
 */
public class TokenStream implements Iterator<Token> {

	private ArrayList<Token> tokenList;
	private int index;
	private int current;

	public TokenStream() {
		this.tokenList = new ArrayList<Token>();
		this.index = -1;
	}

	public void add(Token token)

	{
		this.tokenList.add(token);
	}

	public int size() {
		return this.tokenList.size();
	}

	/**
	 * Method that checks if there is any Token left in the stream with regards
	 * to the current pointer. DOES NOT ADVANCE THE POINTER
	 * 
	 * @return true if at least one Token exists, false otherwise
	 */
	@Override
	public boolean hasNext() {
		// TODO YOU MUST IMPLEMENT THIS
		if (this.index < this.tokenList.size() - 1)
			return true;

		return false;
	}

	/**
	 * Method that checks if there is any Token behind this token in the stream
	 * with regards to the current pointer. DOES NOT ADVANCE THE POINTER
	 * 
	 * @return true if at least one Token exists, false otherwise
	 */

	public boolean hasPrevious() {
		// TODO YOU MUST IMPLEMENT THIS
		if (this.index > 0)
			return true;

		return false;
	}

	/**
	 * Method increments the pointer
	 */

	public void moveNext() {
		// TODO YOU MUST IMPLEMENT THIS
		if (this.index < this.tokenList.size() - 1)
			index++;

	}

	/**
	 * Method decrements the pointer
	 */

	public void moveBack() {
		// TODO YOU MUST IMPLEMENT THIS
		if (this.index > 0)
			index--;

	}

	/**
	 * Method to return the next Token in the stream. If a previous hasNext()
	 * call returned true, this method must return a non-null Token. If for any
	 * reason, it is called at the end of the stream, when all tokens have
	 * already been iterated, return null
	 */
	@Override
	public Token next() {
		// TODO YOU MUST IMPLEMENT THIS
		try {
			this.index++;
			Token t = this.tokenList.get(this.index);
			return t;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Method to remove the current Token from the stream. Note that "current"
	 * token refers to the Token just returned by the next method. Must thus be
	 * NO-OP when at the beginning of the stream or at the end
	 */
	@Override
	public void remove() {
		// TODO YOU MUST IMPLEMENT THIS
		int removeIndex = this.index;
		if (removeIndex >= 0 && removeIndex < tokenList.size()) {
			tokenList.set(removeIndex, null);
		}
	}

	/**
	 * Method to get the previous Token from the stream.
	 */
	public Token previous() {
		// TODO YOU MUST IMPLEMENT THIS
		try {
			this.index--;
			Token t = this.tokenList.get(this.index);
			return t;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Method to save current index
	 */
	public void saveCurrent() {
		// TODO YOU MUST IMPLEMENT THIS
		this.current = this.index;
	}

	/**
	 * Method to reset current index
	 */
	public void setCurrent() {
		// TODO YOU MUST IMPLEMENT THIS
		this.index = this.current;
	}

	/**
	 * Method to reset the stream to bring the iterator back to the beginning of
	 * the stream. Unless the stream has no tokens, hasNext() after calling
	 * reset() must always return true.
	 */
	public void reset() {
		// TODO : YOU MUST IMPLEMENT THIS
		this.index = -1;
		this.current = -1;
		while (this.hasNext()) {
			if (this.next() == null) {
				this.tokenList.remove(index);
				this.index--;
			}
		}

		this.index = -1;
	}

	/**
	 * Method to append the given TokenStream to the end of the current stream
	 * The append must always occur at the end irrespective of where the
	 * iterator currently stands. After appending, the iterator position must be
	 * unchanged Of course this means if the iterator was at the end of the
	 * stream and a new stream was appended, the iterator hasn't moved but that
	 * is no longer the end of the stream.
	 * 
	 * @param stream
	 *            : The stream to be appended
	 */
	public void append(TokenStream stream) {
		// TODO : YOU MUST IMPLEMENT THIS
		if (stream != null) {
			stream.reset();
			int pos = this.tokenList.size();
			if (pos < 0)
				pos = 0;

			while (stream.hasNext()) {
				Token t = stream.next();
				if (t != null) {
					this.tokenList.add(pos, t);
					pos++;
				}
			}
		}
	}

	/**
	 * Method to get the current Token from the stream without iteration. The
	 * only difference between this method and {@link TokenStream#next()} is
	 * that the latter moves the stream forward, this one does not. Calling this
	 * method multiple times would not alter the return value of
	 * {@link TokenStream#hasNext()}
	 * 
	 * @return The current {@link Token} if one exists, null if end of stream
	 *         has been reached or the current Token was removed
	 */
	public Token getCurrent() {
		// TODO: YOU MUST IMPLEMENT THIS
		try {
			Token t = this.tokenList.get(this.index);
			return t;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

}
