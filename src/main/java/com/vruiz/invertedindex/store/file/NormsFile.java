package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.store.codec.Codec;
import com.vruiz.invertedindex.index.CorruptIndexException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage write read of document norms file
 */
public class NormsFile extends TxtFile {


	public NormsFile(String path, Codec codec) {
		this.path = path;
		this.codec = codec;
	}

	@Override
	protected HashMap<?, ?> parseData() throws IOException, CorruptIndexException {
		HashMap<Long, Integer> norms = new HashMap<Long, Integer>();
		String rawData = null;
		//traverse the file and parse one by one the postings of every term using codec
		while ((rawData = this.reader.readLine()) != null) {
			Map.Entry<Long, Integer> entry = this.codec.readEntry(rawData);
			norms.put(entry.getKey(), entry.getValue());
		}
		return norms;
	}

}
