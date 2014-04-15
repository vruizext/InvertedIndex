package com.vruiz.invertedindex.parse;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * StopWordFilterTest
 */
public class StopWordFilterTest {
	@Test
	public void testHasMoreTokens() throws Exception {
		List<String> words = Arrays.asList("a", "able", "about",
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
				"tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "whether",
				"what", "when", "where", "which", "while", "who", "whom", "why",
				"will", "with", "would", "yet", "you", "your");

		MockStream src = new MockStream();
		DataStream out = new StopWordFilter(src);
		for(String w: words) {
			src.setData(w);
			assertTrue("there should be one token", out.hasMoreTokens());
			assertTrue("output not as expected, should be empty for stopwords", out.out().isEmpty());
		}

		src.setData("cool");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertEquals("output not as expected, should be not filtered", "cool", out.out());

	}


	class MockStream extends DataStream {
		public void setData(String data) {
			this.out = data;
		}

		@Override
		public boolean hasMoreTokens() {
			return true;
		}
	}
}
