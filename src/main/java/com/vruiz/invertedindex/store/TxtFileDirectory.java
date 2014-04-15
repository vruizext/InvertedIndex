package com.vruiz.invertedindex.store;

import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Index;
import com.vruiz.invertedindex.index.Posting;
import com.vruiz.invertedindex.index.PostingsDictionary;
import com.vruiz.invertedindex.store.codec.*;
import com.vruiz.invertedindex.store.file.*;
import com.vruiz.invertedindex.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Implements a Directory storing data in text files
 */
public class TxtFileDirectory implements Directory {

	/**
	 * names used to name the files stored in disk
	 */
	protected static final String FIELDS_CONFIG_FILE = "fields";
	protected static final String NORMS_FILE = "norms.";
	protected static final String POSTINGS_FILE = "postings.";
	protected static final String STORED_CONTENT_FILE = "stored.";

	/**
	 * path where the directory files are 
	 */
	protected String directoryPath;
	
	public TxtFileDirectory(String path) {
		this.directoryPath = path;
	}

	/**
	 * save the data stored in the Index to disk
	 * @param index the index containing the data that it's going to be written to disk
	 */
	public void write(Index index) throws IOException, CorruptIndexException {
		HashSet<String> indexedFields = index.getFieldNamesByOption(FieldInfo.INDEXED);
		//for every field store a file
		for(String fieldName: indexedFields) {
			NormsFile fNorms = new NormsFile(this.directoryPath.concat(TxtFileDirectory.NORMS_FILE).concat(fieldName), new NormsCodec());
			fNorms.write(index.getDocumentNorms(fieldName));
			writePostings(index, fieldName);
			indexedFields.add(fieldName);
		}

		Set<String> storedFields = index.getFieldNamesByOption(FieldInfo.STORED);
		//for every stored field
		for(String fieldName: storedFields) {
			StoredFieldsFile fStored = new StoredFieldsFile(this.directoryPath.concat(TxtFileDirectory.STORED_CONTENT_FILE).concat(fieldName), new StoredFieldsCodec());
			fStored.write(index.getStoredDocuments(fieldName));
			storedFields.add(fieldName);
		}
		//to reload index from disk, it's necessary to keep a file with names of the fields that are indexed and stored
		FieldConfigFile fiFile = new FieldConfigFile(this.directoryPath.concat(TxtFileDirectory.FIELDS_CONFIG_FILE), new FieldConfigCodec());
		fiFile.write(index.getFieldNamesByOption());
	}


	/**
	 * write the files containing the postings data. since there's too much info to save it in one single file, every
	 * block is saved to a file, and identified by the first 2 letters of the terms the block is keeping
	 * @param index the index
	 */
	protected void writePostings(Index index, String fieldName) throws IOException, CorruptIndexException {
		//reuse same object to write all files, to reduce memory overhead and avoid problems with GC
		PostingsFile pFile = new PostingsFile("", new PostingsCodec());
		//al files will have this prefix
		String fileName = this.directoryPath.concat(TxtFileDirectory.POSTINGS_FILE).concat(fieldName);
		PostingsDictionary dictionary = index.getPostingsDictionary(fieldName);
		for(Map.Entry entry : dictionary.getPostingsBlocksDictionary().entrySet()) {
			String key = (String)entry.getKey();
			HashMap<String, List<Posting>> block = (HashMap<String, List<Posting>>) entry.getValue();
			//sufix for fileName is the block key
			String blockFileName = fileName.concat("_").concat(key);
			pFile.setPath(blockFileName);
			pFile.write(block);
		}
	}

	/**
	 * Reconstruct the index from the data stored in disk
	 * @return the index with the necessary data to start a search
	 */
	public Index read(Index index) throws IOException, CorruptIndexException {
		//init HashMaps that will keep the index
		HashMap<String, HashMap<Long, Integer>> norms = new HashMap<>();

		HashMap<String, PostingsDictionary> dictionary = new HashMap<>();

		HashMap<String, HashMap<Long, String>> stored = new HashMap<>();

		//now load fields config info
		FieldConfigFile fiFile = new FieldConfigFile(this.directoryPath.concat(TxtFileDirectory.FIELDS_CONFIG_FILE), new FieldConfigCodec());
		HashMap<String, HashSet<String>> fields = (HashMap<String, HashSet<String>>)fiFile.read();

		if(fields == null || fields.isEmpty()) {
			//index can't be reloaded without this data
			return null;
		}

		Set<String> indexedFields = fields.get(FieldInfo.INDEXED);
		for(String fieldName: indexedFields) {
			/*
			 * for every field indexed, need to load norms
			 * if the index would have millions of docs  and several fields, loading this file would take some seconds,
			 * which wouldn't be good...
			 * TODO the norms file could be loaded dynamically like the postings,  whether splitting file in small pieces
			 * TODO and/or with random access to the file instead of traversing it sequentially looking for the docID
			 * TODO but there's a trade-off, if many different files have to be read from disk and parsed, the delay
			 * TODO would be also noticeable when there are many docs to return. It would be necessary a multi-thread
			 * TODO solution that would allow to parse different files in parallel,
			 */
			NormsFile fNorms = new NormsFile(this.directoryPath.concat(TxtFileDirectory.NORMS_FILE.concat(fieldName)), new NormsCodec());
			HashMap<Long,Integer> fieldNorms = (HashMap<Long,Integer>)fNorms.read();
			if (fieldNorms == null || fieldNorms.isEmpty()) {
				Logger.getInstance().error("empty norms file for field: " .concat(fieldName));
				return null;
			}
			norms.put(fieldName, fieldNorms);

			//leave dictionary empty, data will be loaded dynamically when needed
			PostingsDictionary fieldDictionary = new PostingsDictionary();
			dictionary.put(fieldName, fieldDictionary);
		}

		Set<String> storedFields = fields.get(FieldInfo.STORED);
		//for every  stored field, load stored field file
		for(String fieldName: storedFields) {
			/*
			 * The same problem as with the norms... that could be improved loading data on demand, splitting file in
			 * small chunks, parallel access using threads
			 */
			StoredFieldsFile fStored = new StoredFieldsFile(this.directoryPath.concat(TxtFileDirectory.STORED_CONTENT_FILE).concat(fieldName), new StoredFieldsCodec());
			HashMap<Long, String> fieldStored = (HashMap<Long, String>)fStored.read();
			if (fieldStored == null || fieldStored.isEmpty()) {
				Logger.getInstance().error("error reading stored content file ".concat(fieldName));
				return null;
			}
			stored.put(fieldName, fieldStored);
		}
		//at this point, we have already all what we need to start, set data in the index and return it
		index.setNormsByDocument(norms);
		index.setPostingsDictionary(dictionary);
		index.setStoredByDocument(stored);
		index.setFieldNamesByOption(fields);
		return index;
	}

	/**
	 * read a file corresponding to a postings block, parse the data and add it to the dictionary
	 * @param dictionary PostingsDictionary where the data will be loaded
	 * @param fieldName name of the field
	 * @param term which block is going to be loaded
	 * @return the dictionary containing the data extracted from the file
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public PostingsDictionary readPostingsBlock(PostingsDictionary dictionary, String fieldName, String term) throws IOException, CorruptIndexException {
		//al files will have this prefix
		String fileName = this.directoryPath.concat(TxtFileDirectory.POSTINGS_FILE).concat(fieldName);
		//sufix for fileName is the block key;
		String key = dictionary.getKeyForTerm(term);
		String blockFileName = fileName.concat("_").concat(key);
		PostingsFile pFile = new PostingsFile(blockFileName, new PostingsCodec());
		HashMap<String, List<Posting>> postingsBlock = (HashMap<String, List<Posting>>)pFile.read();
		dictionary.getPostingsBlocksDictionary().put(key, postingsBlock);
		return dictionary;
	}

	/**
	 * delete index files
	 */
	public void reset() throws IOException, CorruptIndexException {
		File folder = new File(this.directoryPath);
		//if the directory does not exist, create it
		if (!folder.exists()) {
			folder.mkdir();
			return;
		}
		//if it exists, delete all the files that there are
		File[] files = folder.listFiles();
		for (File f: files) {
			if (f != null && f.exists()) {
				f.delete();
			}
		}
	}

	/**
	 * close open files and resources
	 */
	@Override
	public void close(Index index) {
		HashSet<String> indexedFields = index.getFieldNamesByOption(FieldInfo.INDEXED);
		//for every field store a file
		for(String fieldName: indexedFields) {
			NormsFile fNorms = new NormsFile(this.directoryPath.concat(TxtFileDirectory.NORMS_FILE).concat(fieldName), new NormsCodec());
			fNorms.close();
			PostingsFile pFile = new PostingsFile(this.directoryPath.concat(TxtFileDirectory.POSTINGS_FILE).concat(fieldName), new PostingsCodec());
			pFile.close();
		}
		Set<String> storedFields = index.getFieldNamesByOption(FieldInfo.STORED);
		//for every  stored field, load stored field file
		for(String fieldName: storedFields) {
			PostingsFile fStored = new PostingsFile(this.directoryPath.concat(TxtFileDirectory.STORED_CONTENT_FILE).concat(fieldName), new StoredFieldsCodec());
			fStored.close();
		}
	}

}
