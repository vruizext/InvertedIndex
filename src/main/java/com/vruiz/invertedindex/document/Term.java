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
	 * The word/token or whatever we are indexing
	 */
	private String token;


	public Term(String fieldName, String token) {
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
