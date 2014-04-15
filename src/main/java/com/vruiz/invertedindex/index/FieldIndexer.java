package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.Term;
import com.vruiz.invertedindex.parse.DataStream;
import com.vruiz.invertedindex.parse.TextParser;

import java.util.HashMap;
import java.util.Map;

/**
 * FieldIndexer extracts data from a field and add the extracted terms to the inverted index
 * The aim of this class is allow to encapsulate and isolate the logic for the indexing strategy from other components
 */
public class FieldIndexer {

	Index index;

	public FieldIndexer(final Index index) {
		this.index = index;
	}

	/**
	 * add the contents of one field to the index. There are 2 possible strategies:
	 * 1. extract terms one by one from documents and add them directly to the postings list
	 * 2. build inverted list for a document, then merge this list with the postings list
	 * first approach is slightly faster, since it's saving a loop. nevertheless, this approach wouldn't be valid if
	 * we wanted to use several indexer threads in parallel, (explained in PostingsDictionary.addTerm)
	 * @param field
	 * @param documentId
	 */
	public void addField(final long documentId, final Field field) {
		int docTermsCount = 0;
		int newTermsCount = 0;
		//identifiers or keywords can be indexed without being tokenized
		if (!field.isTokenized()) {
			//just put the content inside a term and add it to the index
			Term term = new Term(field.name(), field.data());
			PostingsDictionary dictionary = index.getPostingsDictionary(term.getFieldName());
			if (dictionary.addTerm(documentId, term)) {
				newTermsCount++;
			}
			docTermsCount = 1;
		} else {
			//get the stream that  provides the terms
			TextParser parser = field.getParser();
			DataStream stream = parser.dataStream(field.name(), field.data());
			stream.start();
			//get the tokens and add to the index
			while (stream.hasMoreTokens()) {
				String token = stream.out();
				if (token.length() > 0) {
					Term term = new Term(field.name(), token);
					PostingsDictionary dictionary = index.getPostingsDictionary(term.getFieldName());
					//add term to postings
					if (dictionary.addTerm(documentId, term)) {
						newTermsCount++;
					}
					docTermsCount++;
				}
			}
			stream.end();
			/*
			this snippet shows how would work the alternative indexing strategy
			Map<String, Short> invertedList = invert(field);
			PostingsDictionary dictionary = index.getPostingsDictionary(field.name());
			newTermsCount += dictionary.mergePostings(documentId, invertedList);
			docTermsCount = invertedList.size();
			*/
		}
		index.numTerms +=  newTermsCount;
		//save the norm of this doc-field
		index.getDocumentNorms(field.name()).put(documentId, docTermsCount);
	}


	/**
	 * Build an inverted list for the terms contained by this Field
	 * @param field
	 * @return
	 */
	public Map<String, Short> invert(final Field field) {
		Map<String, Short> invertedDoc = new HashMap<>();
		TextParser parser = field.getParser();
		DataStream stream = parser.dataStream(field.name(), field.data());
		stream.start();
		//get the tokens and add to the index
		while (stream.hasMoreTokens()) {
			String token = stream.out();
			if (token.length() > 0) {
				Short tf = invertedDoc.get(token);
				int tfVal = 1;
				if (tf != null) {
					tfVal = tf.intValue() + 1;
				}
				invertedDoc.put(token, (short) tfVal);
			}
		}
		stream.end();
		return invertedDoc;
	}


}
