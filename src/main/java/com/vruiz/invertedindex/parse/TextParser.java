package com.vruiz.invertedindex.parse;


/**
 *  TextParser extends Parser and defines in createStreamChain() method a set of rules and filters to split text
 *   in normalized tokens
 */
public class TextParser extends Parser {

	public static final int MIN_LENGTH_DEFAULT = 3;

	public static final int MAX_LENGTH_DEFAULT = 64;


	public TextParser() {

	}

	/**
	 * Here, the filters (and the order they are applied) used to parse the text are defined
	 * @param fieldName this filter chain will be used for this field, to index all documents
	 * @return An StreamChain object which keeps the reference to the first and the last elements in the chain
	 */
	public StreamChain createStreamChain(final String fieldName) {
		Tokenizer src = new Tokenizer();
		DataStream out = new LowerCaseFilter(src);
		out = new StopWordFilter(out);
		out = new AlphaNumericFilter(out);
		out = new LengthFilter(out, MIN_LENGTH_DEFAULT, MAX_LENGTH_DEFAULT);

		return new StreamChain(src, out);
	}



}
