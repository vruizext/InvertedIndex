package com.vruiz.invertedindex.document;

import com.vruiz.invertedindex.index.Tokenizer;
import com.vruiz.invertedindex.util.Logger;

/**
 * A Field belongs to a Document, and is composed by a sequence of Terms. Even though, when the document is created
 * the Terms are indeed still in the form of String
 */
public class Field {

	/**
	 * name of the field
	 */
	private String name;

	/**
	 * data contained by the field, contains the terms to be indexed, and/or stored
	 */
	private String data;

	/**
	 * configuration options for this field
	 */
	private FieldInfo options;



	public Field(String name, String data, FieldInfo options) {
		this.name = name;
		this.data = data;
		this.options = options;
	}

	public Field(String name, String data) {
		this(name, data, new FieldInfo());
	}

	public String name() {
		return name;
	}

	public String data() {
		return this.data;
	}

	public boolean isIndexed() {
		return this.options.isIndexed();
	}

	public boolean isStored() {
		return this.options.isStored();
	}

	public boolean isTokenized() {
		return this.options.isTokenized();
	}

	public void setData(String data) {
		this.data = data;
	}

	/**
	 * Obtain by reflection an instance of the Tokenizer
	 * With this approach, tokenizers can be defined per field, dynamically at run time. The client only needs
	 * to pass the tokenizer class within FieldInfo when the Field is declared.
	 * @return A Tokenizer object which iterates over the data to extract terms
	 */
	public Tokenizer getTokenizer() {
		Class c = this.options.getTokenizer();
		try {
			return (Tokenizer)c.getConstructor(Tokenizer.class).newInstance(this.data);
		} catch (Exception e) {
			Logger.getInstance().error("couldn't create tokenizer object");
		}
		return null;
	}

}

