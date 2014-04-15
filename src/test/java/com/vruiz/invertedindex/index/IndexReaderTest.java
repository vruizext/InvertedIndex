package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.parse.TextParser;
import com.vruiz.invertedindex.store.NullDirectory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * IndexReaderTest
 */
public class IndexReaderTest {


	@Before
	public void setUp() throws Exception {
		IndexWriter iw = new IndexWriter(new NullDirectory());
		iw.reset();

		Document d = new Document(1);
		Field f = new Field("title", "hallo welt", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "hallo test, ist das geil!! tschüß welt test", new FieldInfo(true, false, TextParser.class));
		d.addField(f);
		f = new Field("id", "101", new FieldInfo(false, false));
		d.addField(f);

		iw.addDocument(d);

		d = new Document(2);
		f = new Field("title", "hallo again", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "hello test, this is cool, bye world test!!!", new FieldInfo(true, false, TextParser.class));
		d.addField(f);
		f = new Field("id", "102", new FieldInfo(false, false));
		d.addField(f);
		iw.addDocument(d);
	}

	@Test
	public void testSearch() throws Exception {
		IndexReader reader = new IndexReader(new NullDirectory());
		TreeSet<Hit> hits = reader.search("body", "hello");
		assertEquals("1 hit for hello", 1, hits.size());
		Hit h = (Hit)(hits.toArray())[0];
		assertEquals("hit in document 2", 2, h.document().getDocumentId());

		hits = reader.search("body", "test");
		assertEquals("2 hit for test", 2, hits.size());
		double[] temp = new double[3];
		for(Hit hit: hits) {
			temp[(int)hit.document().getDocumentId()] = hit.score();
		}
		assertTrue("document 2 score should be higher because norm is lower", temp[2] > temp[1]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSearchEmptyTerm() throws Exception {
		IndexReader reader = new IndexReader(new NullDirectory());
		TreeSet<Hit> hits = reader.search("body", "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSearchEmptyField() throws Exception {
		IndexReader reader = new IndexReader(new NullDirectory());
		TreeSet<Hit> hits = reader.search("", "test");
	}
}
