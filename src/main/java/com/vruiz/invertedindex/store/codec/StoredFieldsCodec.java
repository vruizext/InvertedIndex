package com.vruiz.invertedindex.store.codec;


import com.vruiz.invertedindex.index.CorruptIndexException;

import java.util.Map;

/**
 * Handles with the parsing/unparsing of stored fields
 * Format used to store the fields is:
 * {documentId1}:{content1}\n
 * {documentId2}:{content2}\n
 * ...
 *
 */
public class StoredFieldsCodec implements Codec {


	@Override
	public String writeEntry(Map.Entry entry) throws CorruptIndexException {
		Object key = entry.getKey();
		Object val = entry.getValue();

		if (key == null || val == null) {
			throw new CorruptIndexException("corrupted data in entry");
		}
		long documentId = ((Long) key).longValue();
		String content = (String)val;
		if (content.length() == 0) { //if there is no content, it's not necessary to write this to disk
			return "";
		}
		return String.format("%d:%s", documentId, content);
	}

	@Override
	public Map.Entry readEntry(String data) throws CorruptIndexException {
		//split on ":" to get documentId and count
		String[] parts = data.split(":");
		if (parts.length != 2 || parts[0].length() == 0 || parts[1].length() == 0) {
			throw new CorruptIndexException("wrong data format: ".concat(data));
		}

		long documentId = Long.parseLong(parts[0]);
		//stored field can be empty?? it shouldn't be, empty fields are not stored
		return new Codec.Entry<Long,String >(documentId, parts[1]);
	}
}
