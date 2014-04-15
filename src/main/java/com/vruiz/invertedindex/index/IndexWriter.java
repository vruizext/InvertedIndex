package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.Term;
import com.vruiz.invertedindex.store.Directory;

import java.io.IOException;
import java.util.*;

/**
 * This class is used to create an index and add documents
 *
 */
public class IndexWriter {

	private Directory directory;

	private Index index;

	public IndexWriter(Directory directory) {
		this.directory = directory;
		this.index = Index.getInstance();
	}


	/**
	 * Add a document to the in-memory index.
	 * Iterate over all fields and add them to the index
	 */
	public void addDocument(Document doc) {
		doc.setDocumentId(this.index.nextDocumentId());
		Iterator fieldIterator = doc.fields().keySet().iterator();
		while (fieldIterator.hasNext()) {
			String fieldName = (String) fieldIterator.next();
			Field field = doc.fields().get(fieldName);
			//for some fields (like title) we might want store them but not index them
			//skip to index its contents, since it's not in the requirements, even though it would be possible
			if (field.isStored()) {
				this.index.getStoredDocuments(fieldName).put(doc.getDocumentId(), field.data());
			}
			if (field.isIndexed()) {
				this.addField(doc.getDocumentId(), field);
			}
		}
	}

	/**
	 * add the contents of one field to the index
	 * @param field
	 * @param documentId
	 */
	public void addField(long documentId, Field field) {
		int termsCount = 0;
		if (!field.isTokenized()) {
			//if field not tokenized just put the content inside a term and add it to the index
			Term term = new Term(field.name(), field.data());
			addTerm(documentId, term);
			termsCount = 1;
		} else {
			//get a tokenizer which will provide the terms
			Tokenizer st = field.getTokenizer();
			String token = null;
			//get the tokens and add to the index
			while ((token = st.nextTerm()) != null) {
				//create a Term
				if (!token.isEmpty()) {
					Term term = new Term(field.name(), token);
					addTerm(documentId, term);
					termsCount++;
				}
			}
		}
		//save the norm of this doc-field
		this.index.getDocumentNorms(field.name()).put(documentId, termsCount);
	}

	/**
	 * add a single term to the index
	 * 1. create postings list if this is the first occurrence of the term
	 * 1. add documentId to the postings list, if not present
	 * 2. increase term frequency if do we have already a posting
	 * @param documentId
	 * @param term
	 */
	private void addTerm(long documentId, Term term) {
		//get the dictionary for this field
		PostingsDictionary dictionary = this.index.getPostingsDictionary(term.getFieldName());
		//check if there is any postings list for this term,
		LinkedList<Posting> postingsList = dictionary.getPostingsList(term.getToken());
		if (postingsList != null) {
			//if there's already a posting of this term for this document, it hast to be the last one added
			//to the posting list, since we are working sequentially and single thread
			//in other case, we would need to traverse the list to find if there's a posting with this docId
			//... or we should count terms occurrences while we tokenize/parse
			Posting posting = postingsList.getLast();
			if(posting.getDocumentId() != documentId) {
				//add new posting element to list if this is the first occurrence in this document
				posting = new Posting(documentId);
				postingsList.add(posting);
			}
			//increase term frequency
			posting.addOccurrence();
		} else {
			//if first occurrence of this term in the index, create a new postings list
			postingsList = new LinkedList<Posting>();
			//add posting element to list
			Posting posting = new Posting(documentId);
			posting.addOccurrence();
			postingsList.add(posting);
			//set the postings list in the hash map
			dictionary.addPostingsList(term.getToken(), postingsList);
			this.index.newTerm();
		}
	}

	public long getNumDocs() {
		return this.index.numDocs;
	}

	public long getNumTerms() {
		return this.index.numTerms;
	}

	/**
	 * flush all indexed documents to disk
	 */
	public void flush() throws IOException, CorruptIndexException {
		this.directory.write(this.index);
	}

	public void reset() throws IOException, CorruptIndexException {
		this.directory.reset();
		this.index.reset();
	}

	public void close() {
		this.directory.close(this.index);
	}
}



