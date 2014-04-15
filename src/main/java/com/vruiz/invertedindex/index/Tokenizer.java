package com.vruiz.invertedindex.index;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Get tokens from a String, adding functionality to clean and normalize the words
 */
public class Tokenizer  {

	private final static List<String> stopwords = Arrays.asList("a", "able", "about",
													"across", "after", "all", "almost", "also", "am", "among", "an",
													"and", "any", "are", "as", "at", "be", "because", "been", "but",
													"by", "can", "cannot", "could", "dear", "did", "do", "does",
													"either", "else", "ever", "every", "for", "from", "get", "got",
													"had", "has", "have", "he", "her", "hers", "him", "his", "how",
													"however", "i", "if", "in", "into", "is", "it", "its", "just",
													"least", "let", "like", "likely", "may", "me", "might", "most",
													"must", "my", "neither", "no", "nor", "not", "of", "off", "often",
													"on", "only", "or", "other", "our", "own", "rather", "said", "say",
													"says", "she", "should", "since", "so", "some", "than", "that",
													"the", "their", "them", "then", "there", "these", "they", "this",
													"tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
													"what", "when", "where", "which", "while", "who", "whom", "why",
													"will", "with", "would", "yet", "you", "your");

	private static final String EMPTY_STRING = "";

	private StringTokenizer st = null;

	public Tokenizer() {
	}

	public Tokenizer(String str) {
		this.st = new StringTokenizer(str);
	}

	/**
	 * filter common stopwords, it helps to save space and improve performance
	 * @param word word being processed
	 * @return if the current word is filtered, return the next word in the string, if not, return current word
	 */
	private String stopWordsFilter(String word) {
		if (Tokenizer.stopwords.contains(word)) {
			return Tokenizer.EMPTY_STRING;
		}
		return word;
	}

	/**
	 * filter words shorter than minLength
	 * @param minLength minimum length allowed
	 * @param word word that is being processed
	 * @return if the current word is filtered, return the next word in the string, if not, return current word
	 */
	private String lengthFilter(int minLength, String word) {
		if (word.length() < minLength) {
			return Tokenizer.EMPTY_STRING;
		}
		return word;
	}

	/**
	 * remove non-alphanumeric characters and set to lower case
	 * @param word the word
	 * @return filtered text
	 */
	private String normalize(String word) {
		String lower = word.toLowerCase();
		String filter =  lower.replaceAll("[^a-z0-9äöüßáàéèìíóòúù]", Tokenizer.EMPTY_STRING);
		return filter;
	}

	/**
	 * get next element of the string and normalize it before returning it
	 * @return the normalized, filtered word, ready to be indexed
	 */
	public String nextTerm() {
		if (this.st.hasMoreTokens()) {
			String token = (String) this.st.nextElement();
			token = stopWordsFilter(normalize(token));
			token = lengthFilter(2, token);
			return token;
		}

		return null;
	}
}
