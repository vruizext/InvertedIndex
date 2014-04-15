package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.index.CorruptIndexException;
import org.junit.Test;

import java.util.Formatter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * NormsCodecTest
 */
public class NormsCodecTest {
	Formatter formatter = new Formatter();

	@Test
	public void testWriteEntry() throws Exception {
		long docId = 1;
		int norm = 2;
		Map.Entry entry = new Codec.Entry<Long,Integer >(docId, norm);
		NormsCodec codec = new NormsCodec();
		codec.writeEntry(formatter, entry);
		String data = formatter.toString();
		String expected = String.format("%d:%d\n", docId, norm);
		assertEquals("codec is not writing properly ", expected, data);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyNorm() throws CorruptIndexException {
		long docId = 1;
		Map.Entry entry = new Codec.Entry<Long,Integer >((long)1, null);
		NormsCodec codec = new NormsCodec();
		codec.writeEntry(formatter, entry);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyId() throws CorruptIndexException {
		int count = 1;
		Map.Entry entry = new Codec.Entry<Long,Integer >(null, count);
		NormsCodec codec = new NormsCodec();
		codec.writeEntry(formatter, entry);
	}

	@Test
	public void testReadEntry() throws CorruptIndexException {
		int docId = 1;
		int count = 2;
		String rawData = String.format("%d:%d", docId, count);
		NormsCodec codec = new NormsCodec();
		Map.Entry<Long,Integer > entry = codec.readEntry(rawData);
		assertTrue("key shouldn't be null", entry.getKey() != null);
		assertTrue("value shouldn't be null", entry.getValue() != null);
		assertEquals("docId doesn't match", docId, entry.getKey().intValue());
		assertEquals("norm doesn't match", count, entry.getValue().intValue());
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryWrongFormatNoId() throws CorruptIndexException {
		String rawData = ":22";
		NormsCodec codec = new NormsCodec();
		Map.Entry<Long,Integer > entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryWrongFormatNoNorm() throws CorruptIndexException {
		String rawData = "22:";
		NormsCodec codec = new NormsCodec();
		Map.Entry<Long,Integer > entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryWrongFormatNoDelimiter() throws CorruptIndexException {
		String rawData = "22";
		NormsCodec codec = new NormsCodec();
		Map.Entry<Long,Integer > entry = codec.readEntry(rawData);
	}
}
