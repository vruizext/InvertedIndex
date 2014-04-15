package com.vruiz.invertedindex.parse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * LowerCaseFilterTest
 */
public class LowerCaseFilterTest  {
	@Test
	public void testHasMoreTokens() throws Exception {
		MockStream src = new MockStream();
		DataStream out = new LowerCaseFilter(src);
		src.setData("ABC");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertEquals("output not filtered as expected", "abc", out.out());
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