package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.CorruptIndexException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Map;

/**
 * FieldConfigCodecTest
 */
public class FieldConfigCodecTest {
	Formatter formatter = new Formatter();
	@Test
	public void testWriteEntry() throws Exception {
		String[] indexedFields = {"id", "body"};
		HashSet<String> indexed = new HashSet<>();
		indexed.add(indexedFields[0]);
		indexed.add(indexedFields[1]);
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry entry = new Codec.Entry<>(FieldInfo.INDEXED, indexed);
		codec.writeEntry(formatter, entry);
		String data = formatter.toString();
		String expected = "indexed:id,body,\n";
		assertEquals("codec is not writing properly ", expected, data);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyKey() throws Exception {
		String[] indexedFields = {"body", "id"};
		HashSet<String> indexed = new HashSet<>(Arrays.asList(indexedFields));
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry entry = new Codec.Entry<>(null, indexed);
		codec.writeEntry(formatter, entry);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptySet() throws Exception {
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry entry = new Codec.Entry<String, HashSet<String>>(FieldInfo.INDEXED, null);
		codec.writeEntry(formatter, entry);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryWrongElementInSet() throws Exception {
		String[] indexedFields = {"body", "id"};
		HashSet<String> indexed = new HashSet<>(Arrays.asList(indexedFields));
		indexed.add("");
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry entry = new Codec.Entry<>(FieldInfo.INDEXED, indexed);
		codec.writeEntry(formatter, entry);
	}

	@Test
	public void testReadEntry() throws Exception {
		String data = "indexed:body,id,";
		String[] indexedFields = {"body", "id"};
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry<String, HashSet<String>> entry = codec.readEntry(data);
		assertTrue("key shouldn't be null", entry.getKey() != null);
		assertTrue("value shouldn't be null", entry.getValue() != null);
		assertEquals("key doesn't match", FieldInfo.INDEXED, entry.getKey());
		HashSet<String> fieldsList = entry.getValue();
		assertEquals("data not properly parsed, wrong length", indexedFields.length, fieldsList.size());
		assertTrue("first expected field not present in the list", fieldsList.contains(indexedFields[0]));
		assertTrue("second expected field not present in the list", fieldsList.contains(indexedFields[1]));
	}

	public void testReadEntryWrongFormatNoFieldType() throws CorruptIndexException {
		String data = ":body,id,";
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry<String, HashSet<String>> entry = codec.readEntry(data);
	}

	public void testReadEntryWrongFormatWrongFieldName() throws CorruptIndexException {
		String data = "indexed:,id,";
		FieldConfigCodec codec = new FieldConfigCodec();
		Map.Entry<String, HashSet<String>> entry = codec.readEntry(data);
	}
}
