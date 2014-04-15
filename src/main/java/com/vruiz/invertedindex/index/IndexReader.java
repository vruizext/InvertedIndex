package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Term;
import com.vruiz.invertedindex.parse.TextParser;
import com.vruiz.invertedindex.parse.DataStream;
import com.vruiz.invertedindex.store.Directory;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import com.vruiz.invertedindex.util.Logger;

import java.io.IOException;
import java.util.*;

/**
 * IndexReader provides access to read the index and perform queries
 */
public class IndexReader {

	/**
	 * provides access to the files where the index data is stored
	 */
	private Directory directory;

	/**
	 * index data is kept inside this data structure
	 */
	private Index index;

	public IndexReader(Directory directory) {
		this.directory = directory;
		this.index = Index.getInstance();
	}

	/**
	 * open the index for reading, ie, load data from directory
	 */
	public void open() throws IOException, CorruptIndexException {
		directory.read(index);
	}

	/**
	 * close the index and all opened resources
	 */
	public void close() {
		directory.close(index);
	}

	/**
	 * search for occurrences of a single word in the specified field
	 * @param word the word which is being searched for
	 * @param fieldName in which field is the word being searched
	 * @return a set of Hits with the documents that have been matched
	 */
	public TreeSet<Hit> search(final String fieldName, String word) throws IOException, CorruptIndexException {
		if (fieldName.isEmpty() || word.isEmpty()) {
			throw new IllegalArgumentException("fieldName and word are required: " .concat(word). concat(" "). concat(fieldName));
		}
		//use tokenizer that we read only one single word and to normalize input word in same way that indexed terms
		TextParser parser = new TextParser();
		DataStream stream = parser.dataStream(fieldName, word);
		stream.start();
		if (!stream.hasMoreTokens()) {
			stream.end();
			throw new IllegalArgumentException("the word introduced is not searchable in this index: ".concat(word));
		}
		word = String.copyValueOf(stream.out().toCharArray());
		stream.end();
		return this.query(new Term(fieldName, word));
	}

	/**
	 * Perform a query for a single term. Returns a Set of Hits
	 * @param term Term that is being searched
	 * @return Documents that match are returned as Hit objects within a Set
	 */
	private TreeSet<Hit> query(final Term term) throws IOException, CorruptIndexException {
		TreeSet<Hit> hits = new TreeSet<Hit>();

		LinkedList<Posting> postingsList = lookupData(term);

		if (postingsList == null) {
			//term was not found  an empty result
			return hits;
		}

		//traverse posting list,  get list of results and score the docs
		for (Posting p : postingsList) {
			//get the norm to calculate the score of this hit,
			int normValue = index.getDocumentNorms(term.getFieldName()).get(p.getDocumentId());
			//score the hit proportionally to the ratio tf/norm
			//use sqrt to compress the range of scores, log could be also be used...
			double score = Math.sqrt((double)p.getTermFrequency() / normValue);
			//read stored content
			Document doc = index.document(p.getDocumentId());

			//add hit to the list
			hits.add(new Hit(doc, (float) score));
		}

		return hits;
	}

	/**
	 * try to load the postings list for the given term, first from memory, if not in memory
	 * load from fisk
	 * @param term the term searched
	 * @return postings list for the term, if any occurrence is found
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	private LinkedList<Posting> lookupData(final Term term) throws IOException, CorruptIndexException {
		//get the dictionary for this field
		PostingsDictionary dictionary = index.getPostingsDictionary(term.getFieldName());
		if (dictionary == null) {
			//this field is not indexed, so we can't search
			Logger.getInstance().error(String.format("field %s is not indexed, hence, not searchable" , term.getFieldName()));
			return null;
		}
		//first check if there is any postings list for this term already in memory
		LinkedList<Posting> postingsList = dictionary.getPostingsList(term.getToken());
		if (postingsList == null) {
			//if not, try to load from disk
			dictionary = ((TxtFileDirectory)directory).readPostingsBlock(dictionary, term.getFieldName(), term.getToken());
			postingsList = dictionary.getPostingsList(term.getToken());
		}

		return postingsList;
	}

}
