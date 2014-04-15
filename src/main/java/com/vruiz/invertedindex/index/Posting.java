package com.vruiz.invertedindex.index;


/**
 * A Posting represents an occurrence of a Term inside a document:
 */
public class Posting {

	/**
	 * document where the term occurs
	 */
	private long documentId;

	/**
	 * number of times that the term occurs in the document
	 */
	private short termFrequency;

	/**
	 * @param documentId documentId
	 * @param termFrequency term frequency
	 */
	public Posting(long documentId, short termFrequency) {
		this.documentId = documentId;
		this.termFrequency = termFrequency;
	}

	public Posting(long documentId) {
		this(documentId, (short)0);
	}

	public long getDocumentId() {
		return documentId;
	}

	public int getTermFrequency() {
		return termFrequency;
	}

	public void addOccurrence() {
		this.termFrequency++;
	}

	public String toString() {
		return String.format("%d,%d", this.documentId, this.termFrequency);
	}
}
