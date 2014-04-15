package com.vruiz.invertedindex.parse;

/**
 * LowerCaseFilter normalize the token to lower case
 */
public class LowerCaseFilter extends DataStream{
	public LowerCaseFilter(DataStream input) {
		super(input);
	}

	@Override
	public boolean hasMoreTokens() {
		if (!input.hasMoreTokens()) {
			out = null;
			return false;
		}
		if (!input.out().isEmpty()) {
			out = input.out().toLowerCase();
		}
		return true;
	}
}
