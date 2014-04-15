package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Posting;

import java.util.*;

/**
 * Handles how to write a postings list to string and how to parse from string
 * The postings list for a single term has the following format;
 * {term1}:{postingList1}\n
 * {term2}:{postingList2}\n
 * ....
 *
 * The format of the postingList is;
 * {documentId1, termFrequency1};{documentId2, termFrequency2}; ...
 */
public class PostingsCodec implements Codec {


	@Override
	public String writeEntry(Map.Entry entry) throws CorruptIndexException {
		Object key = entry.getKey();
		Object val = entry.getValue();

		if (key == null || val == null) {
			throw new CorruptIndexException("corrupted data in map entry");
		}
		String term = (String) key;
		LinkedList<Posting> postings = (LinkedList<Posting>) val;
		String out = String.format("%s:",term);
		for(Posting p: postings) {
			if (p == null) {
				throw new CorruptIndexException("corrupted data in posting entry");
			}
			out = out.concat(String.format("%s;", p.toString()));
		}
		return out;
	}

	@Override
	public Map.Entry readEntry(String data) throws CorruptIndexException {
		//first we have to split on ":" to get the term
		String[] parts = data.split(":");
		if (parts.length != 2 || parts[0].length() == 0 || parts[1].length() == 0) {
			throw new CorruptIndexException("wrong data format: ".concat(data));
		}
		String term = parts[0];
		//now we have the postings list, each posting delimited by semicolon
		parts  = parts[1].split(";");
		if (parts.length == 0) {
			throw new CorruptIndexException("wrong data format: ".concat(data));
		}
		LinkedList<Posting> postingsList = new LinkedList<Posting>();
		for(String p: parts) {
			//for every posting list, values are delimited by comma
			String[] postingStr = p.split(",");
			if (postingStr.length != 2 || postingStr[0].length() == 0 || postingStr[1].length() == 0) {
				throw new CorruptIndexException("wrong data format: ".concat(data));
			}
			long documentId = Long.parseLong(postingStr[0]);
			short termFreq = Short.parseShort(postingStr[1]);
			//construct a posting from the read values
			Posting posting = new Posting(documentId, termFreq);
			postingsList.add(posting);
		}
		//create a new Entry and return it
		return new Codec.Entry<String, LinkedList<Posting>>(term, postingsList);
	}


}
