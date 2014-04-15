package com.vruiz.invertedindex.store.file;

/**
 * Handles exceptions occurred when handling with index files
 */
public class FileException extends Exception {

	public FileException(String msg) {
		super(msg);
	}

	public FileException(Exception e) {
		super(e);
	}
}
