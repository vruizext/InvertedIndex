package com.vruiz.invertedindex.parse;

import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * DataParserTest
 */
public class TextParserTest extends TestCase {

	public void testDataStream() throws Exception {
		Field field = new Field("test", "Test, simpLe forğ $%$§%$&%parser", new FieldInfo(true, false, TextParser.class));
		TextParser parser = field.getParser();
		DataStream stream = parser.dataStream(field.name(), field.data());
		stream.start();
		assertTrue("there should be one token", stream.hasMoreTokens());
		assertEquals("output not filtered as expected", "test", stream.out());
		assertTrue("there should be one token", stream.hasMoreTokens());
		assertEquals("output not filtered as expected", "simple", stream.out());
		assertTrue("there should be one token", stream.hasMoreTokens());
		assertEquals("output not filtered as expected", "for", stream.out());
		assertTrue("there should be one token", stream.hasMoreTokens());
		assertEquals("output not filtered as expected", "parser", stream.out());
	}


}
