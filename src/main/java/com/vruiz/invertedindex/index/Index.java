package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;

import java.util.*;

/**
 * Inverted Index, modeled using a postings lists,  which  maps terms occurrences to documents
 */
public class Index {

	/**
	 * keep a list of the field names  of the fields that are indexed and another for the stored
	 */
	protected HashMap<String, HashSet<String>> fieldNamesByOption = new HashMap<>();

	/**
	 * For every indexed field,  keep a postings dictionary
	 * A postings dictionary keep list of terms-postings list
	 * In a terms-postings list,for every indexed Term we keep a list of Postings
	 */
	protected HashMap<String, PostingsDictionary> postingsDictionary = new HashMap<>();

	/**
	 * norms of every docId-fieldName
	 */
	protected HashMap<String, HashMap<Long, Integer>> normsByDocument = new HashMap<>();

	/**
	 * For every stored field, we have a HashMap with documentId as key and the stored field as value
	 */
	protected HashMap<String, HashMap<Long, String>> storedByDocument = new HashMap<>();


	/**
	 * number of documents indexed. used to assign unique Ids to the documents
	 */
	protected long numDocs = 0;

	/**
	 * number of terms indexed
	 */
	protected long numTerms = 0;


	private static Index instance = null;

	/**
	 * use singleton to have only one instance of the Index, just in case both IndexWriter and IndexReader
	 * are used simultaneously, they should access the same data
	 * @return
	 */
	public static Index getInstance() {
		if (instance == null) {
			instance = new Index();
		}
		return instance;
	}

	private Index() {
		//to protect from direct instantiation
	}


	public long getNumDocs() {
		return numDocs;
	}

	public long getNumTerms() {
		return numTerms;
	}

	/**
	 * when a new doc is created, increase numDocs and return the value as Id
	 * @return new value for the count, which will be used as Id for next document
	 */
	public long nextDocumentId() {
		return ++this.numDocs;
	}

	/**
	 * if a new term is added to the dictionary, increase the counter
	 * @return the new value for the count
	 */
	public long newTerm() {
		return ++this.numTerms;
	}

	/**
	 * Get postings dictionary for the given field
	 * If there's any defined, set up a new one
	 * @param fieldName
	 * @return
	 */
	public PostingsDictionary getPostingsDictionary(final String fieldName) {
		PostingsDictionary dictionary = this.postingsDictionary.get(fieldName);
		if (dictionary == null) {
			dictionary = new PostingsDictionary();
			this.postingsDictionary.put(fieldName, dictionary);
		}
		return dictionary;
	}


	/**
	 * Get document norms for fieldName
	 * If there's any defined, set up a new one
	 * @param fieldName
	 * @return
	 */
	public HashMap<Long, Integer> getDocumentNorms(final String fieldName) {
		HashMap<Long, Integer> norms = this.normsByDocument.get(fieldName);
		if (norms == null) {
			norms = new HashMap<>();
			this.normsByDocument.put(fieldName, norms);
		}
		return norms;
	}

	/**
	 * Get list of documents stored fields for  fieldName
	 * If there's any defined, set up a new one
	 * @param fieldName
	 * @return
	 */
	public HashMap<Long, String> getStoredDocuments(final String fieldName) {
		HashMap<Long, String> stored = this.storedByDocument.get(fieldName);
		if (stored == null) {
			stored = new HashMap<>();
			this.storedByDocument.put(fieldName, stored);
		}
		return stored;
	}

	/**
	 * get fields which have configured the  option
	 * @return a set containing fields which have configured the specified option
	 */
	public HashSet<String> getFieldNamesByOption(final String fieldOption) {
		HashSet<String> fields = this.fieldNamesByOption.get(fieldOption);
		if (fields == null) {
			if (fieldOption.equals(FieldInfo.INDEXED)) {
				if (!this.postingsDictionary.isEmpty()) {
					Set<String> keySet = this.postingsDictionary.keySet();
					//this is a bit confusing... but the jvm can't cast directly from keySet to HashSet ;(
					fields = new HashSet<>(Arrays.asList(keySet.toArray(new String[0])));
				} else {
					fields = new HashSet<>();
				}
			} else if (fieldOption.equals(FieldInfo.STORED)) {
				if (!this.storedByDocument.isEmpty()) {
					Set<String> keySet = this.storedByDocument.keySet();
					fields = new HashSet<>(Arrays.asList(keySet.toArray(new String[0])));
				} else {
					fields = new HashSet<>();
				}
			}
			this.fieldNamesByOption.put(fieldOption, fields);
		}
		return fields;
	}


	public void setPostingsDictionary(HashMap<String, PostingsDictionary> postingsDictionary) {
		this.postingsDictionary = postingsDictionary;
	}

	public HashMap<String, HashSet<String>> getFieldNamesByOption() {
		return this.fieldNamesByOption;
	}

	public void setNormsByDocument(HashMap<String, HashMap<Long, Integer>> normsByDocument) {
		this.normsByDocument = normsByDocument;
	}

	public void setStoredByDocument(HashMap<String, HashMap<Long, String>> storedByDocument) {
		this.storedByDocument = storedByDocument;
	}

	public void setFieldNamesByOption(HashMap<String, HashSet<String>> fieldNamesByOption) {
		this.fieldNamesByOption = fieldNamesByOption;
	}

	/**

	 * get a list of the field names from which we have content stored
	 * @return
	 */
	public HashSet<String> getStoredFields() {
		return this.fieldNamesByOption.get(FieldInfo.STORED);
	}

	/**
	 * get a list of the field names from which we have text indexed
	 * @return
	 */
	public HashSet<String> getIndexedFields() {
		return this.fieldNamesByOption.get(FieldInfo.INDEXED);
	}

	/**
	 * retrieves all stored fields for the given documentId
	 * @param documentId
	 * @return Document containing retrieved data
	 */
	public Document document(final long documentId) {
		Document doc = new Document(documentId);
		for(String fieldName: storedByDocument.keySet()) {
			HashMap<Long, String> storedData = this.storedByDocument.get(fieldName);
			if (storedData.containsKey(documentId)) {
				Field f = new Field(fieldName, storedData.get(documentId));
				doc.addField(f);
			}
		}
		return doc;
	}

	/**
	 * deletes all data in the index, reset the object to its initial state
	 */
	public void reset() {
		this.numDocs = 0;
		this.numTerms = 0;
		this.fieldNamesByOption.clear();
		this.postingsDictionary.clear();
		this.normsByDocument.clear();
		this.storedByDocument.clear();
	}


}
