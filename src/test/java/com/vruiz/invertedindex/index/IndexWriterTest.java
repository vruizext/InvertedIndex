package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by bik on 4/3/14.
 */
public class IndexWriterTest {
	@Test
	public void testAddDocument() throws Exception {
		//create indexer and ensure that index is totally empty before start
		String path = new File("").getAbsolutePath().concat("/index/");
		IndexWriter iw = new IndexWriter(new TxtFileDirectory(path));
		iw.reset();

		Document d = new Document(1);
		Field f = new Field("title", "hallo welt", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "hallo, ist das geil, hello??", new FieldInfo(true, false, Tokenizer.class));
		d.addField(f);
		f = new Field("id", "101", new FieldInfo(false, false));
		d.addField(f);

		iw.addDocument(d);

		d = new Document(2);
		f = new Field("title", "hallo again", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "hello, this is cool, hello!!!", new FieldInfo(true, false, Tokenizer.class));
		d.addField(f);
		f = new Field("id", "102", new FieldInfo(false, false));
		d.addField(f);
		iw.addDocument(d);

		IndexReader ir = new IndexReader(new TxtFileDirectory(path));
		TreeSet<Hit> hits = ir.search("body", "hello");
		Iterator it = hits.descendingSet().iterator();
		int i = 1;
		while(it.hasNext()) {
			Hit hit = (Hit) it.next();
			System.out.printf("%d - %f - %s \n", i, hit.score(), hit.document().fields().get("title").data());
			i++;

		}
	}

	@Test
	public void testAddField() throws Exception {

	}

	@Test
	public void testGetNumDocs() throws Exception {

	}

	@Test
	public void testGetNumTerms() throws Exception {

	}

	@Test
	public void testFlush() throws Exception {

	}
}
