package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.index.Posting;
import com.vruiz.invertedindex.store.codec.PostingsCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * PostingsFileTest
 */
public class PostingsFileTest {
	String folder = "resources/test-files/";

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testRead() throws Exception {
		String path = folder.concat("postings.test.write");
		PostingsFile pFile = new PostingsFile(path, new PostingsCodec());
		HashMap<String, List<Posting>> postings = (HashMap<String, List<Posting>>)pFile.read();
		assertEquals("there should be 2 entries loaded in the map", 2, postings.size());
	}

	@Test
	public void testWrite() throws Exception {
		//build map and insert data
		HashMap<String, List<Posting>> postings = new HashMap<>();
		LinkedList<Posting> postingsList = new LinkedList<>();
		postingsList.add(new Posting((long)1, (short)2));
		postingsList.add(new Posting((long)4, (short)3));
		postings.put("avocado", postingsList);

		postingsList = new LinkedList<>();
		postingsList.add(new Posting((long)2, (short)1));
		postingsList.add(new Posting((long)5, (short)4));
		postings.put("guacamole", postingsList);

		//delete the file to ensure we don't have data from previous tests
		String path = folder.concat("postings.test.write");
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		//write data to file
		PostingsFile pFile = new PostingsFile(path, new PostingsCodec());
		pFile.write(postings);
		//compare with the expected output
		String textWritten = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		String textExpected = new String(Files.readAllBytes(Paths.get(folder.concat("postings.test.read"))), StandardCharsets.UTF_8);
		assertEquals("data was not written as expected", textExpected, textWritten);
	}
}
