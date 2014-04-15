package com.vruiz.invertedindex.parse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * TokenizerTest
 */
public class TokenizerTest  {
	@Test
	public void testHasMoreTokens() throws Exception {
		Tokenizer src = new Tokenizer();
		src.setData("hello world, let's go!");
		src.start();
		assertTrue("there should be one token", src.hasMoreTokens());
		assertEquals("output not filtered as expected", "hello", src.out());
		assertTrue("there should be one token", src.hasMoreTokens());
		assertEquals("output not filtered as expected", "world,", src.out());
		assertTrue("there should be one token", src.hasMoreTokens());
		assertEquals("output not filtered as expected", "let's", src.out());
		assertTrue("there should be one token", src.hasMoreTokens());
		assertEquals("output not filtered as expected", "go!", src.out());
	}

}
