package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.index.Posting;
import com.vruiz.invertedindex.store.codec.Codec;
import com.vruiz.invertedindex.index.CorruptIndexException;

import java.io.IOException;
import java.util.*;

/**
 * Handles Read/Write of postings file
 */
public class PostingsFile extends TxtFile {


	public PostingsFile(String path, Codec codec) {
		this.path = path;
		this.codec = codec;
	}


	protected HashMap<?,?> parseData() throws IOException, CorruptIndexException {
		HashMap<String, List<Posting>> postings = new HashMap<String, List<Posting>>();
		String rawData = null;
		//traverse the file and parse one by one the postings of every term using codec
		while ((rawData = this.reader.readLine()) != null) {
			Map.Entry<String, List<Posting>> entry = this.codec.readEntry(rawData);
			if (entry != null) {
				postings.put(entry.getKey(), entry.getValue());
			}
		}
		return postings;
	}

}
