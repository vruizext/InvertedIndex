package com.vruiz.invertedindex.parse;

import java.util.regex.Pattern;

/**
 * AlphaNumericFilter filters out characters that are not letters (occidental languages) or numbers
 */
public class AlphaNumericFilter extends DataStream {

	protected static final Pattern FILTER = Pattern.compile("[^a-z0-9äöüàéèìíóòúùâêîôûß]");

	public AlphaNumericFilter(DataStream input) {
		this.input = input;
	}


	@Override
	public boolean hasMoreTokens() {
		if (!input.hasMoreTokens()) {
			out = null;
			return false;
		}
		if (input.out().isEmpty()) {
			out = Tokenizer.EMPTY_STRING;
		} else {
			out = FILTER.matcher(input.out).replaceAll(Tokenizer.EMPTY_STRING);
		}
		return true;
	}
}
