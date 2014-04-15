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
	protected HashMap<String, HashSet<String>> fieldNamesByOption = new HashMap<String, HashSet<String>>();

	/**
	 * For every indexed field,  keep a map of Terms-Postings list.
	 * In a terms-postings list,for every indexed Term we keep a list of Postings
	 */
//	protected HashMap<String, HashMap<String, List<Posting>>> postingsByTerm = new HashMap<String, HashMap<String, List<Posting>>>();

	/**
	 * For every indexed field,  keep a postings dictionary
	 * A postings dictionary keep list of terms-postings list
	 * In a terms-postings list,for every indexed Term we keep a list of Postings
	 */
	protected HashMap<String, PostingsDictionary> postingsDictionary = new HashMap<String, PostingsDictionary>();

	/**
	 * norms of every docId-fieldName
	 */
	protected HashMap<String, HashMap<Long, Integer>> normsByDocument = new HashMap<String, HashMap<Long, Integer>>();

	/**
	 * For every stored field, we have a HashMap with documentId as key and the stored field as value
	 */
	protected HashMap<String, HashMap<Long, String>> storedByDocument = new HashMap<String,HashMap<Long, String>>();


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
	public PostingsDictionary getPostingsDictionary(String fieldName) {
		PostingsDictionary dictionary = this.postingsDictionary.get(fieldName);
		if (dictionary == null) {
			dictionary = new PostingsDictionary();
			this.postingsDictionary.put(fieldName, dictionary);
		}
		return dictionary;
	}

	/**
	 * Get postings lists for a field
	 * If there's any defined, set up a new one
	 * @param fieldName
	 * @return
	 */
//	public HashMap<String, List<Posting>> getTermPostings(String fieldName) {
//		HashMap<String, List<Posting>> postings = this.postingsByTerm.get(fieldName);
//		if (postings == null) {
//			postings = new HashMap<String, List<Posting>>();
//			this.postingsByTerm.put(fieldName, postings);
//		}
//		return postings;
//	}

	/**
	 * Get document norms for fieldName
	 * If there's any defined, set up a new one
	 * @param fieldName
	 * @return
	 */
	public HashMap<Long, Integer> getDocumentNorms(String fieldName) {
		HashMap<Long, Integer> norms = this.normsByDocument.get(fieldName);
		if (norms == null) {
			norms = new HashMap<Long, Integer>();
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
	public HashMap<Long, String> getStoredDocuments(String fieldName) {
		HashMap<Long, String> stored = this.storedByDocument.get(fieldName);
		if (stored == null) {
			stored = new HashMap<Long, String>();
			this.storedByDocument.put(fieldName, stored);
		}
		return stored;
	}

	/**
	 * get
	 * @return a set containing fields which have configured the specified option
	 */
	public HashSet<String> getFieldNamesByOption(String fieldOption) {
		HashSet<String> fields = this.fieldNamesByOption.get(fieldOption);
		if (fields == null) {
			if (fieldOption.equals(FieldInfo.INDEXED)) {
				if (!this.postingsDictionary.isEmpty()) {
					Set<String> keySet = this.postingsDictionary.keySet();
					//this is a bit confusing... but the jvm can't cast directly from keySet to HashSet ;(
					fields = new HashSet<String>(Arrays.asList(keySet.toArray(new String[0])));
				} else {
					fields = new HashSet<String>();
				}
			} else if (fieldOption.equals(FieldInfo.STORED)) {
				if (!this.storedByDocument.isEmpty()) {
					Set<String> keySet = this.storedByDocument.keySet();
					fields = new HashSet<String>(Arrays.asList(keySet.toArray(new String[0])));
				} else {
					fields = new HashSet<String>();
				}
			}
			this.fieldNamesByOption.put(fieldOption, (HashSet<String>)fields);
		}
		return fields;
	}


	public void setPostingsDictionary(HashMap<String, PostingsDictionary> postingsDictionary) {
		this.postingsDictionary = postingsDictionary;
	}

	public HashMap<String, HashSet<String>> getFieldNamesByOption() {
		return this.fieldNamesByOption;
	}

//	public void setPostingsByTerm(HashMap<String, HashMap<String, List<Posting>>> postingsByTerm) {
//		this.postingsByTerm = postingsByTerm;
//	}

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
	public Document document(long documentId) {
		Document doc = new Document();
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
