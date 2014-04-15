package com.vruiz.invertedindex.store;

import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Index;

import java.io.IOException;

/**
 * Empty implementation, it does not write neither read files, just used for tests
 */
public class NullDirectory implements Directory {
	/**
	 * save the data stored in the Index to disk
	 *
	 * @param index
	 */
	@Override
	public void write(Index index) throws IOException, CorruptIndexException {

	}

	/**
	 * Reconstruct the index from the data stored in disk
	 *
	 * @param index
	 * @return
	 */
	@Override
	public Index read(Index index) throws IOException, CorruptIndexException {
		return Index.getInstance();
	}

	/**
	 * reset the index, ie, delete all files stored in disk
	 */
	@Override
	public void reset() throws IOException, CorruptIndexException {

	}

	/**
	 * close open files and resources
	 *
	 * @param index
	 */
	@Override
	public void close(Index index) {

	}
}
