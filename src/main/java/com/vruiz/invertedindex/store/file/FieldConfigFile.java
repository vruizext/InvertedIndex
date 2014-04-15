package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.store.codec.Codec;
import com.vruiz.invertedindex.index.CorruptIndexException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Write/Read info about fields config to/from disk
 */
public class FieldConfigFile extends TxtFile {


	public FieldConfigFile(String path, Codec codec) {
		this.path = path;
		this.codec = codec;
	}

	protected  HashMap<?, ?> parseData() throws IOException, CorruptIndexException {
		HashMap<String, HashSet<String>> fields = new HashMap<>();
		String rawData = null;
		//traverse the file and parse one by one the postings of every term using codec
		while ((rawData = this.reader.readLine()) != null) {
			Map.Entry<String, HashSet<String>> entry = this.codec.readEntry(rawData);
			if (entry != null) {
				fields.put(entry.getKey(), entry.getValue());
			}
		}
		return fields;
	}

}
