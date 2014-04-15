package com.vruiz.invertedindex.parse;

import java.util.regex.Pattern;

/**
 * A Tokenizer gets the input from a Reader and splits data in token
 */
public class Tokenizer extends DataStream {

	protected static final Pattern SEPARATOR = Pattern.compile("\\s+");

	protected static final String EMPTY_STRING = "";

	protected String input = null;

	protected String[] buffer;

	protected int count;

	protected int minLength;

	protected int maxLength;

	public Tokenizer() {
		this.minLength = TextParser.MIN_LENGTH_DEFAULT;
		this.maxLength = TextParser.MAX_LENGTH_DEFAULT;
	}

	public Tokenizer(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	public void setData(String data) {
		this.input = data;
	}

	public void start() {
		buffer = SEPARATOR.split(input);
		count = 0;
	}

	public void end() {
		input = null;
		buffer = null;
		count = 0;

	}


	protected boolean nextToken() {
		if (count >= buffer.length) {
			out = Tokenizer.EMPTY_STRING;
			return false;
		}
		out = buffer[count++];

		if(out.length() < minLength || out.length() > maxLength) {
			out = Tokenizer.EMPTY_STRING;
			return nextToken();
		}
		return true;
	}

	/**
	 * get next element of the string and normalize it before returning it
	 * @return the normalized, filtered word, ready to be indexed
	 */
	public boolean hasMoreTokens() {
		if (buffer.length == 0) {
			out = Tokenizer.EMPTY_STRING;
			return  false;
		}
		return nextToken();
	}
}
