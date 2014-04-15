package com.vruiz.invertedindex.parse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * AlphaNumericFilterTest
 */
public class AlphaNumericFilterTest {
	@Test
	public void testHasMoreTokens() throws Exception {
		MockStream src = new MockStream();
		DataStream out = new AlphaNumericFilter(src);
		src.setData("!§$%&a/()=?");
		assertTrue("there should be one token", out.hasMoreTokens());
		assertEquals("output not as expected, there should be only an a", "a", out.out());
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
