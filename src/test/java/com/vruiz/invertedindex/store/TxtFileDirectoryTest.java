package com.vruiz.invertedindex.store;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.Index;
import com.vruiz.invertedindex.index.IndexWriter;
import com.vruiz.invertedindex.index.Posting;
import com.vruiz.invertedindex.parse.TextParser;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TxtFileDirectoryTest
 */
public class TxtFileDirectoryTest {

	String folder = "resources/test-files/directory_";

	/**
	 * generate some dummy data to fill the index
	 * @param pathSuffix path where the index is created
	 * @return the index writer used to add the documents
	 * @throws Exception
	 */
	protected IndexWriter initIndexFixtures(String pathSuffix) throws Exception {
		String path = folder.concat(pathSuffix);
		IndexWriter iw = new IndexWriter(new TxtFileDirectory(path));
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

		d = new Document(3);
		f = new Field("title", "hallo dritte mal", new FieldInfo(false, true));
		d.addField(f);
		f = new Field("body", "hola test, esto mola, ciao mundo test!!!", new FieldInfo(true, false, TextParser.class));
		d.addField(f);
		f = new Field("id", "103", new FieldInfo(false, false));
		d.addField(f);
		iw.addDocument(d);
		return iw;
	}


	@Test
	public void testWrite() throws Exception {
		//write index data to disk using directory
		IndexWriter iw = initIndexFixtures("write/");
		String path = folder.concat("write/");
		TxtFileDirectory dir = new TxtFileDirectory(path);
		Index index = Index.getInstance();
		dir.write(index);
		dir.close(index);
		//read written data with a new directory object
		 dir = new TxtFileDirectory(path);
		dir.read(index);
		assertEquals("there should be 3 documents", 3, index.getNumDocs());
		assertEquals("document 1 is ok?", "hallo welt", index.document(1).fields().get("title").data());

		dir.readPostingsBlock(index.getPostingsDictionary("body"), "body", "test");
		LinkedList<Posting> postings = index.getPostingsDictionary("body").getPostingsList("test");
		assertEquals("test should have  3 documents postings", 3, postings.size());
		for (Posting p: postings) {
			assertEquals("2 occurrences in each document", 2, p.getTermFrequency());
		}

	}

	@Test
	public void testRead() throws Exception {
		//read the index from disk using directory
//		IndexWriter iw = initIndexFixtures("read/");
//		iw.flush();iw.close();
		String path = folder.concat("read/");
		TxtFileDirectory dir = new TxtFileDirectory(path);
		Index index = Index.getInstance();
		dir.read(index);

		//check that data is as expected
		assertEquals("document 1 is ok?", "hallo welt", index.document(1).fields().get("title").data());

		dir.readPostingsBlock(index.getPostingsDictionary("body"), "body", "test");
		LinkedList<Posting> postings = index.getPostingsDictionary("body").getPostingsList("test");
		assertEquals("test should have  3 documents postings", 3, postings.size());
		for (Posting p: postings) {
			assertEquals("2 occurrences in each document", 2, p.getTermFrequency());
		}
	}

	@Test
	public void testReset() throws Exception {
		IndexWriter iw = initIndexFixtures("write/");
		String path = folder.concat("write/");
		Directory dir = new TxtFileDirectory(path);
		Index index = Index.getInstance();
		dir.write(index);
		dir.close(index);

		dir = new TxtFileDirectory(path);
		dir.reset();

		File folder = new File(path);
		File[] files = folder.listFiles();
		assertEquals("there shouldnt be any files here", 0, files.length);
	}
}
