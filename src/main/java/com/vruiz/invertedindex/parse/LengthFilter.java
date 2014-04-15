package com.vruiz.invertedindex.parse;

/**
 * LengthFiter, filter out tokens which length is not between minLength and maxLength
 */
public class LengthFilter extends DataStream {
	protected int minLength;

	protected int maxLength;

	public LengthFilter(DataStream input, int minLength, int maxLength) {
		super(input);
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	@Override
	public boolean hasMoreTokens() {
		if (!input.hasMoreTokens()) {
			out = null;
			return false;
		}

		if(input.out().length() < minLength || input.out().length() > maxLength) {
			out = Tokenizer.EMPTY_STRING;
		} else {
			out = input.out();
		}
		return true;
	}
}
