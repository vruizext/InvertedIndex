package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Posting;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.Map;

/**
 * PostingsCodecTest
 */
public class PostingsCodecTest {
	Formatter formatter = new Formatter();

	private Map.Entry entry;

	@Before
	public void setUp() throws Exception {
		String term = "avocado";
		LinkedList<Posting> postingsList = new LinkedList<Posting>();
		postingsList.add(new Posting((long)1, (short)2));
		postingsList.add(new Posting((long)4, (short)3));
		entry = new Codec.Entry<>(term, postingsList);
	}

	@Test
	public void testWriteEntry() throws Exception {
		PostingsCodec codec = new PostingsCodec();
		codec.writeEntry(formatter, entry);
		String rawData = formatter.toString();
		String expected = "avocado:1,2;4,3;\n";
		assertEquals("codec is not writing properly", expected, rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyTerm() throws Exception {
		PostingsCodec codec = new PostingsCodec();
		entry = new Codec.Entry<>(null, (LinkedList<Posting>)this.entry.getValue());
		codec.writeEntry(formatter, entry);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyList() throws Exception {
		PostingsCodec codec = new PostingsCodec();
		entry = new Codec.Entry<>((String)entry.getKey(), null);
		codec.writeEntry(formatter, entry);
	}

	@Test(expected = CorruptIndexException.class)
	public void testWriteEntryEmptyListNode() throws Exception {
		LinkedList<Posting> postingsList = (LinkedList<Posting>)this.entry.getValue();
		postingsList.add(null);
		PostingsCodec codec = new PostingsCodec();
		codec.writeEntry(formatter, entry);
	}

	@Test
	public void testReadEntry() throws Exception {
		String rawData = "avocado:1,2;4,3;";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);

		assertTrue("key shouldn't be null", entry.getKey() != null);
		assertTrue("value shouldn't be null", entry.getValue() != null);
		assertEquals("docId doesn't match", this.entry.getKey(), entry.getKey());

		LinkedList<Posting> expectedList = (LinkedList<Posting>)this.entry.getValue();
		LinkedList<Posting> list = entry.getValue();
		assertEquals("posting list not properly parsed, wrong length", expectedList.size(), list.size());

		Posting expectedPosting = expectedList.getFirst();
		Posting posting = list.getFirst();
		assertEquals("posting doesn't match ", expectedPosting.toString(), posting.toString());

		expectedPosting = expectedList.getLast();
		posting = list.getLast();
		assertEquals("posting doesn't match ", expectedPosting.toString(), posting.toString());
	}


	@Test(expected = CorruptIndexException.class)
	public void testReadEntryNoTerm() throws Exception {
		String rawData = ":1,2;4;3;";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryNoList() throws Exception {
		String rawData = "avocado:";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	 public void testReadEntryWrongPosting1() throws Exception {
		String rawData = "avocado:1;2;4;3;";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryWrongPosting2() throws Exception {
		String rawData = "avocado:1,2,4;3;";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadEntryWrongPosting3() throws Exception {
		String rawData = "avocado:1,2,4;3;";
		PostingsCodec codec = new PostingsCodec();
		Map.Entry<String, LinkedList<Posting>> entry = codec.readEntry(rawData);
	}

}
