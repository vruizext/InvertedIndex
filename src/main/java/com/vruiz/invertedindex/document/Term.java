package com.vruiz.invertedindex.document;

/**
 * Represent a single word(token) contained in a document, also called term,
 */
public class Term {

	/**
	 * document field where this term occurs
	 */
	private String fieldName;

	/**
	 * The word/token or whatever it's indexed/stored
	 */
	public String token;


	public Term(final String fieldName, final String token) {
		this.token = token;
		this.fieldName = fieldName;
	}


	public String getToken() {
		return token;
	}

	public String getFieldName() {
		return fieldName;
	}

}
