package com.vruiz.invertedindex.index;

/**
 * This exception is thrown when some inconsistency in the index is found
 */
public class CorruptIndexException extends Exception{
	public CorruptIndexException(String msg) {
		super(msg);
	}

	public CorruptIndexException(Exception e) {
		super(e);
	}
}
