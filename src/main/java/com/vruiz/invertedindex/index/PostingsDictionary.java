package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.util.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * To be more efficient at search, this class splits the dictionary in smaller blocks, Every of this blocks will
 * be saved to disk as a single file. Thus, is not necessary reloading the whole index to search for a single term
 */
public class PostingsDictionary {

	/**
	 * Terms are grouped in blocks. For every block, the 2 first characters of the term are used as key
	 * For every block of terms,  keep a map with Term as key and Postings list as value.
	 * In a terms-postings list,for every indexed Term we keep a list of Postings
	 */
	protected HashMap<String, HashMap<String, List<Posting>>> postingsBlocksByTermKey = new HashMap<String, HashMap<String, List<Posting>>>();


	/**
	 * Get postings lists for a term
	 * 1. Get the block from the 2 first characters of the term
	 * 2. Search in the block for the term and get its postings
	 * @param term term that is being searched
	 * @return
	 */
	public LinkedList<Posting> getPostingsList(String term) {
		String termKey = getKeyForTerm(term);
		HashMap<String, List<Posting>> block = this.getPostingsBlock(termKey);

		//lookup in the termsPostings list and return the postings for this term
		return (LinkedList<Posting>)block.get(term);
	}

	/**
	 * Add a posting list to its correspondent block
	 * @param term term being indexed, used to resolve the key for the block
	 * @param postings list of Postings
	 */
	public void addPostingsList(String term, List<Posting> postings) {
		String termKey = getKeyForTerm(term);
		HashMap<String, List<Posting>> block = this.getPostingsBlock(termKey);
		block.put(term, postings);
	}

	/**
	 * get the block where this term is mapped, from the two first characters of the term
	 * If block is not yet created for this prefix, add a new one
	 * @param term a term being searched or indexed
	 * @return a map containing the postings list for this block
	 */
	public HashMap<String, List<Posting>> getPostingsBlock(String term) {
		String termKey = getKeyForTerm(term);
		HashMap<String, List<Posting>> postingsByKey = this.postingsBlocksByTermKey.get(termKey);
		//if there's still no data in this block, create a new map and add it to postingsTermsByKey
		if (postingsByKey == null) {
			postingsByKey = new HashMap<String, List<Posting>>();
			this.postingsBlocksByTermKey.put(term, postingsByKey);
		}
		return postingsByKey;
	}

	/**
	 * get the dictionary containing all blocks of postings lists
	 * @return
	 */
	public HashMap<String, HashMap<String, List<Posting>>> getPostingsBlocksDictionary() {
		return this.postingsBlocksByTermKey;
	}

	/**
	 * use the two first characters as key to split the dictionary in smaller parts
	 * @param term
	 * @return
	 */
	public String getKeyForTerm(String term) {
		return term.substring(0,2);
	}
}
