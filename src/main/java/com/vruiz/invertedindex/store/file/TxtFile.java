package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.store.codec.Codec;
import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.*;

/**
 * Provides basic operations to write/read files
 * Subclasses must implement write and read methods
 */
public abstract class TxtFile {
	BufferedWriter writer = null;
	BufferedReader reader =  null;

	/**
	 * path to the file
	 */
	protected String path;

	/**
	 * Codec knows how to encode and decode the index data to write/read in/from disk
	 */
	protected Codec codec;


	public void setPath(String path) {
		this.path = path;
	}

	protected void openReader() throws IOException {
		Path p = Paths.get(this.path);
		if (Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
			this.reader = Files.newBufferedReader(Paths.get(this.path), StandardCharsets.UTF_8);
		}
	}

	protected void openWriter() throws IOException {
		Path p = Paths.get(this.path);
		if (Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
			this.writer = Files.newBufferedWriter(Paths.get(this.path), StandardCharsets.UTF_8, WRITE, APPEND);
		} else {
			this.writer = Files.newBufferedWriter(Paths.get(this.path), StandardCharsets.UTF_8, CREATE_NEW, WRITE);
		}
	}

	protected void closeReader() throws IOException {
		this.reader.close();
	}

	protected void closeWriter() throws IOException {
		this.writer.close();
	}

	public void close() {
		try {
			if (this.writer != null) {
				this.writer.close();
			}
			if (this.reader != null) {
				this.reader.close();
			}
		} catch (IOException e) {/*nothing to do here if closing the file fails...*/}
	}

	protected abstract HashMap<?, ?> parseData() throws IOException, CorruptIndexException;


	public  HashMap<?, ?> read() throws IOException, CorruptIndexException {
		this.openReader();
		if (this.reader != null) {
			HashMap<?, ?> map = parseData();
			this.closeReader();
			return map;

		}
		return null;
	}


	public void write(HashMap<?, ?> map) throws IOException, CorruptIndexException {
		this.openWriter();
		if (this.writer != null) {
			//iterate over entries, write one by one using the codec to transform data to String
			for(Map.Entry entry : map.entrySet()) {
				String data = this.codec.writeEntry(entry);
				this.writer.write(data);
				this.writer.newLine();
			}
			this.closeWriter();
		}
	}

	public void delete() {
		try {
			File f = new File(this.path);
			if (f.exists()) {
				f.delete();
			}
		} catch (Exception e) {
			Logger.getInstance().error("file could not be deleted ".concat(this.path), e);
		}

	}

}
