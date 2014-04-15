package com.vruiz.invertedindex.document;

import java.util.HashMap;

/**
 * Represents a single document contained in the index
 */
public class Document {

	/**
	 * unique identifier for the document in the index
	 * has to be long, integers have 32 bits in Java, the max doc Id the index could have would be  32767
	 */
	private long documentId;

	/**
	 * A document is composed of Fields. Every field is identified by its name (String)
	 */
	private final HashMap<String, Field> fields = new HashMap<>();


	public Document(long documentId) {
		this.setDocumentId(documentId);
	}

	public Document() {
		this.setDocumentId(-1);
	}

	public HashMap<String, Field> fields() {
		return this.fields;
	}

	public void addField(final Field field) {
		this.fields.put(field.name(), field);
	}

	public long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}
}
