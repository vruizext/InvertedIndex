package com.vruiz.invertedindex.parse;

import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import junit.framework.TestCase;

/**
 * DataParserTest
 */
public class TextParserTest extends TestCase {

	public void testDataStream() throws Exception {
		Field field = new Field("test", "Test, simpLe forğ $%$§%$&%parser", new FieldInfo(true, false, TextParser.class));
		TextParser parser = field.getParser();
		DataStream stream = parser.dataStream(field.name(), field.data());
		stream.start();
		while (stream.hasMoreTokens()) {
			System.out.println(stream.out());
		}
	}

	public void testCreateStreamChain() throws Exception {

	}
}
