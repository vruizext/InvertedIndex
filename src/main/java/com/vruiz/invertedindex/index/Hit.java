package com.vruiz.invertedindex.index;


import com.vruiz.invertedindex.document.Document;

/**
 * Represents one document hit when searching for a term in the index
 */
public class Hit implements Comparable<Hit> {

	/**
	 * document that has been hit
	 */
	private Document document;

	/**
	 * score assigned to this document (used for document retrieval)
	 */
	private float score;

	public Hit(final Document doc, final float score) {
		this.document = doc;
		this.score = score;
	}

	public Document document() {
		return this.document;
	}

	public float score() {
		return score;
	}

	@Override
	public int compareTo(Hit hit) {
		if (this.score == hit.score) {
			return 0;
		} else if (this.score > hit.score) {
			return 1;
		}
		return -1;
	}
}
