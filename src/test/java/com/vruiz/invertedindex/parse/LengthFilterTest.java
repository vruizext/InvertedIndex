package com.vruiz.invertedindex.parse;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * LengthFilterTest
 */
public class LengthFilterTest {
	@Test
	public void testHasMoreTokens() throws Exception {
		MockStream src = new MockStream();
		DataStream out = new LengthFilter(src, TextParser.MIN_LENGTH_DEFAULT, TextParser.MAX_LENGTH_DEFAULT);
		src.setData("a");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertTrue("since it'S only 1 char, output should be empty", out.out().isEmpty());

		src.setData("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertTrue("since it'S too long, output should be empty", out.out().isEmpty());

		src.setData("aaaa");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertEquals("output should not be empty", "aaaa", out.out());
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
