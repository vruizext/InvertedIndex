package com.vruiz.invertedindex.parse;

import java.util.regex.Pattern;

/**
 * A Tokenizer gets the input from a Reader and splits data in tokens. The StreamChain always has to begin with
 * a tokenizer that splits the text and streams the token to the next elements in the chain
 */
public class Tokenizer extends DataStream {

	/**
	 * this tokenizer splits text on whitespaces
	 */
	protected static final Pattern SEPARATOR = Pattern.compile("\\s+");

	/**
	 * keep an empty string to return it whenever it's needed, rather than creating a new one every time
	 */
	protected static final String EMPTY_STRING = "";

	/**
	 * text that is going to be processed
	 */
	protected String input = null;

	/**
	 * tokens are hold here until they are processed
	 */
	protected String[] buffer;

	/**
	 * keep the count of tokens processed
	 */
	protected int count;

	/**
	 * minimum length for a token to be processed, shorter token are not being processed
	 */
	protected int minLength;

	/**
	 * max length for a token to be processed, longer tokens are discarded
	 */
	protected int maxLength;

	public Tokenizer() {
		this.minLength = TextParser.MIN_LENGTH_DEFAULT;
		this.maxLength = TextParser.MAX_LENGTH_DEFAULT;
	}

	public Tokenizer(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	/**
	 * Tokenizer object can be reused, just set new data, and then call start()
	 * @param data data to be processed
	 */
	public void setData(String data) {
		this.input = data;
	}

	/**
	 * split the text and fill the buffer with the tokens
	 */
	public void start() {
		buffer = SEPARATOR.split(input);
		count = 0;
	}

	/**
	 * reset everything so the object can be reused for next document
	 */
	public void end() {
		input = null;
		buffer = null;
		count = 0;

	}

	/**
	 * extract next token and set it in the output, so next element in the chain can process it
	 * @return
	 */
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
	 * get next element of the string pass it to next element in the chain
	 * @return true if there's some token to be processed, false in other case
	 */
	public boolean hasMoreTokens() {
		if (buffer.length == 0) {
			out = Tokenizer.EMPTY_STRING;
			return  false;
		}
		return nextToken();
	}
}
