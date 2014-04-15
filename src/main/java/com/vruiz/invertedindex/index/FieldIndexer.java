package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.Term;
import com.vruiz.invertedindex.parse.DataStream;
import com.vruiz.invertedindex.parse.TextParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * FieldProcessor
 */
public class FieldIndexer {

	Index index;

	public FieldIndexer(Index index) {
		this.index = index;
	}

	/**
	 * add the contents of one field to the index
	 * @param field
	 * @param documentId
	 */
	public void addField(long documentId, Field field) {
		int termsCount = 0;
		//identifiers or keywords can be indexed without being tokenized
		if (!field.isTokenized()) {
			//just put the content inside a term and add it to the index
			Term term = new Term(field.name(), field.data());
			PostingsDictionary dictionary = index.getPostingsDictionary(term.getFieldName());
			dictionary.addTerm(documentId, term);
			termsCount = 1;
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
					dictionary.addTerm(documentId, term);
					termsCount++;
				}
			}
			stream.end();
		}
		//save the norm of this doc-field
		index.getDocumentNorms(field.name()).put(documentId, termsCount);
	}

	/**
	 *
	 * @param field
	 * @return
	 */
	public Map<String, Short> invert(Field field) {
		Map<String, Short> invertedDoc = new HashMap<>();
		//get a tokenizer which will provide the terms
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
