package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.store.codec.StoredFieldsCodec;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by bik on 4/5/14.
 */
public class StoredFieldsFileTest {

	String folder = "testfiles/";

	@Test
	public void testRead() throws Exception {
		StoredFieldsFile fStored = new StoredFieldsFile(folder.concat("stored.test.read"), new StoredFieldsCodec());
		HashMap<Long, String> storedFields = (HashMap<Long, String>)fStored.read();
		assertEquals("there should be three entries loaded in the map", 3, storedFields.size());
		assertTrue("key 1 not read", storedFields.containsKey((long) 1));
		assertTrue("key 2 not read", storedFields.containsKey((long) 2));
		assertTrue("key 3 not read", storedFields.containsKey((long) 3));
		assertEquals("wrong value for key 1 ", "hello soundcloud", (String) storedFields.get((long) 1));
		assertEquals("wrong value for key 2 ", "this is an stored field", (String) storedFields.get((long) 2));
		assertEquals("wrong value for key 3 ", "what else is there?", (String) storedFields.get((long)3));
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongData() throws Exception {
		StoredFieldsFile fStored = new StoredFieldsFile(folder.concat("stored.wrong.read"), new StoredFieldsCodec());
		HashMap<Long, String> storedFields = (HashMap<Long, String>)fStored.read();
	}

	@Test
	public void testWrite() throws Exception {
		HashMap<Long, String> storedFields = new HashMap<Long, String>();
		storedFields.put((long)1,"hello soundcloud");
		storedFields.put((long)2,"this is an stored field");
		storedFields.put((long) 3, "what else is there?");

		String path = folder.concat("stored.test.write");
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		StoredFieldsFile fStored = new StoredFieldsFile(path, new StoredFieldsCodec());
		fStored.write(storedFields);

		String textWritten = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		String textExpected = new String(Files.readAllBytes(Paths.get(folder.concat("stored.test.read"))), StandardCharsets.UTF_8);
		assertEquals("data was not written as expected", textExpected, textWritten);
	}
}
