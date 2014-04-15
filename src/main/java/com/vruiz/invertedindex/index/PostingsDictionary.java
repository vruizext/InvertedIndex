package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Term;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class maintains the inverted index data structure
 * To be more efficient at search, the dictionary is splited in small blocks, Every of the blocks will
 * be saved to disk as a single file. Thus, is not necessary reloading the whole index to search for a single term, but
 * only the file containing the searched term
 */
public class PostingsDictionary {

	/**
	 * Terms are mapped to blocks, using the first 3 digits of the string's hash code
	 * For every block of terms,  keep a map with Term as key and Postings list as value.
	 * In a terms-postings list,for every indexed Term we keep a list of Postings
	 */
	protected final HashMap<String, HashMap<String, List<Posting>>> postingsBlocksByTermKey = new HashMap<>();


	/**
	 * Get postings lists for a term
	 * 1. Get the block
	 * 2. Search in the block for the term and get its postings
	 * @param term term that is being searched
	 * @return
	 */
	public LinkedList<Posting> getPostingsList(final String term) {
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
	public void addPostingsList(final String term, final List<Posting> postings) {
		String termKey = getKeyForTerm(term);
		HashMap<String, List<Posting>> block = this.getPostingsBlock(termKey);
		block.put(term, postings);
	}

	/**
	 * get the block where this term is mapped,
	 * If block is not yet created, add a new one
	 * @param termKey key where this term is  mapped
	 * @return a map containing the postings list for this block
	 */
	public HashMap<String, List<Posting>> getPostingsBlock(final String termKey) {
		HashMap<String, List<Posting>> postingsByKey = this.postingsBlocksByTermKey.get(termKey);
		//if there's still no data in this block, create a new map and add it to postingsTermsByKey
		if (postingsByKey == null) {
			postingsByKey = new HashMap<>();
			this.postingsBlocksByTermKey.put(termKey, postingsByKey);
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
	 * use the 3 first digits of hash code to map terms to blocks
	 * @param term
	 * @return the key
	 */
	public String getKeyForTerm(final String term) {
		int hash = Math.abs(term.hashCode()); //hashes can be negative!! take absolute value
		return Integer.toString(hash).substring(0, 3);
	}

	/**
	 * add a single term to the index dictionary
	 * 1. create postings list if this is the first occurrence of the term
	 * 1. add documentId to the postings list, if not present
	 * 2. increase term frequency if do we have already a posting
	 * @param documentId
	 * @param term
	 * @return true when the term was just added to the dictionary, ie, there was still no postings list for it
	 */
	public boolean addTerm(final long documentId, final Term term) {
		//check if there is any postings list for this term,
		LinkedList<Posting> postingsList = getPostingsList(term.getToken());
		if (postingsList != null) {
			//if there's already a posting of this term for this document, it has to be the last one added
			//to the posting list, since we are working sequentially and single thread
			//in other case, we would need to traverse the list to find if there's a posting with this docId
			//... or we should first invert every document and then merge with the postings dictionary
			Posting posting = postingsList.getLast();
			if(posting.getDocumentId() != documentId) {
				//add new posting element to list if this is the first occurrence in this document
				posting = new Posting(documentId);
				postingsList.add(posting);
			}
			//increase term frequency
			posting.addOccurrence();
			return false;
		}
		postingsList = new LinkedList<>();
		//add posting element to list
		Posting posting = new Posting(documentId);
		posting.addOccurrence();
		postingsList.add(posting);
		//set the postings list in the hash map
		addPostingsList(term.getToken(), postingsList);
		return true;
	}

	/**
	 *
	 * @param documentId document id
	 * @param inverted inverted postings list of the document
	 * @return number of new terms added to the dictionary
	 */
	protected int mergePostings(final long documentId, final Map<String, Short> inverted) {
		int count = 0;
		for(Map.Entry entry : inverted.entrySet()) {
			String term = (String)entry.getKey();
			Short tf = (Short)entry.getValue();
			Posting posting = new Posting(documentId, tf);
			//check if there is any postings list for this term,
			LinkedList<Posting> postingsList = getPostingsList(term);
			if (postingsList == null) {
				//if not, create a new list and add term to dictionary
				postingsList = new LinkedList<>();
				addPostingsList(term, postingsList);
				count++;
			}
			postingsList.add(posting);
		}
		return count;
	}
}
