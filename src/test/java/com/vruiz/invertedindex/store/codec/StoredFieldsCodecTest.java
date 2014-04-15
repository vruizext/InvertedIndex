package com.vruiz.invertedindex.store.codec;

import static org.junit.Assert.*;

import com.vruiz.invertedindex.index.CorruptIndexException;
import org.junit.Test;

import java.util.Formatter;
import java.util.Map;

/**
 * StoredFieldsCodecTest
 */
public class StoredFieldsCodecTest {
	Formatter formatter = new Formatter();

	@Test
	public void testWriteEntry() throws Exception {
		long docId = 1;
		String text = "ich liebe guacamole";
		Map.Entry entry = new Codec.Entry<>(docId, text);
		StoredFieldsCodec codec = new StoredFieldsCodec();
		codec.writeEntry(formatter, entry);
		String data = formatter.toString();
		String expected = String.format("%d:%s\n", docId, text);
		assertEquals("codec is not writing properly ", expected, data);
	}

	@Test
	public void testWriteEntryEmptyText() throws Exception {
		long docId = 1;
		String text = "";
		Map.Entry entry = new Codec.Entry<>(docId, text);
		StoredFieldsCodec codec = new StoredFieldsCodec();
		codec.writeEntry(formatter, entry);
		String data = formatter.toString();
		assertTrue("for an empty text nothing should be written", data.length() == 0);
	}

	@Test
	public void testReadEntry() throws Exception {
		long docId = 1;
		String text = "ich liebe guacamole";
		String rawData = String.format("%d:%s", docId, text);
		StoredFieldsCodec codec = new StoredFieldsCodec();
		Map.Entry<Long,String > entry = codec.readEntry(rawData);
		assertEquals("documentId doesn't match", docId, entry.getKey().longValue());
		assertEquals("text doesn't match", text, entry.getValue());
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongFormatNoDocId() throws CorruptIndexException {
		String rawData = ":hello work";
		StoredFieldsCodec codec = new StoredFieldsCodec();
		Map.Entry<Long,String > entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongFormatNoContent() throws CorruptIndexException {
		String rawData = "1:";
		StoredFieldsCodec codec = new StoredFieldsCodec();
		Map.Entry<Long,String > entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongFormatNoDelimiter() throws CorruptIndexException {
		String rawData = "1this is not making sense";
		StoredFieldsCodec codec = new StoredFieldsCodec();
		Map.Entry<Long,String > entry = codec.readEntry(rawData);
	}
}
