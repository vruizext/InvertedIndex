package com.vruiz.invertedindex.index;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.parse.TextParser;
import com.vruiz.invertedindex.parse.Tokenizer;
import com.vruiz.invertedindex.store.NullDirectory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

/**
 * IndexWriterTest
 */
public class IndexWriterTest {
	@Test
	public void testAddDocument() throws Exception {
		//create indexer and ensure that index is totally empty before start
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

		Index index = Index.getInstance();
		PostingsDictionary dictionary = index.getPostingsDictionary("body");
		List<Posting> postingsList = dictionary.getPostingsList("geil");
		for(Posting p: postingsList) {
			assertEquals("only one posting in document 1 is expected", 1, p.getDocumentId());
			assertEquals("TF is expected to be 1", 1, p.getTermFrequency());
		}

		postingsList = dictionary.getPostingsList("test");
		assertEquals("there should be 2 postings of this term", 2, postingsList.size());

		for(Posting p: postingsList) {
			assertEquals("TF is expected to be 2", 2, p.getTermFrequency());
		}

		HashMap<Long, Integer> norms =  index.getDocumentNorms("body");
		assertEquals("2 documents norms are expected", 2, norms.size());
		assertEquals("document 1 has 8 terms", 8, norms.get((long)1).intValue());
		assertEquals("document 1 has 7 terms", 6, norms.get((long)2).intValue());

		HashMap<Long, String> stored =  index.getStoredDocuments("title");
		assertEquals("2 documents titles stored are expected", 2, stored.size());
		assertEquals("title for document 1 is not ok", "hallo welt", stored.get((long)1));
		assertEquals("title for document 2 is not ok", "hallo again", stored.get((long)2));

	}

	@Test
	public void testAddEmptyDocument() throws Exception {
		IndexWriter iw = new IndexWriter(new NullDirectory());
		iw.reset();

		Document d = new Document(1);
		Field f = new Field("title", "", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "", new FieldInfo(true, false, TextParser.class));
		d.addField(f);
		f = new Field("id", "", new FieldInfo(false, false));
		d.addField(f);
		//empty document is accepted, but any  postings are added to the index
		//TODO users should validate input before calling IndexWriter.addDocument? or should it be validated by IndexWriter?
		iw.addDocument(d);

		Index index = Index.getInstance();
		HashMap<Long, Integer> norms =  index.getDocumentNorms("body");
		assertEquals("1 documents norms is expected", 1, norms.size());
		assertEquals("document 1 has 0 terms", 0, norms.get((long)1).intValue());

		HashMap<Long, String> stored =  index.getStoredDocuments("title");
		assertEquals("1 documents titles stored is expected", 1, stored.size());
		assertEquals("title for document 1 is not ok", "", stored.get((long)1));
	}

}
