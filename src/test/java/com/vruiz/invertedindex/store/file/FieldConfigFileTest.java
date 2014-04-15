package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.store.codec.FieldConfigCodec;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * FieldConfigFileTest
 */
public class FieldConfigFileTest {

	String folder = "resources/test-files/";

	@Test
	public void testRead() throws Exception {
		FieldConfigFile fiFile = new FieldConfigFile(folder.concat("fields.read"), new FieldConfigCodec());
		HashMap<String, HashSet<String>> fields = (HashMap<String, HashSet<String>>)fiFile.read();
		assertEquals("there should be 2 entries loaded in the fields map", 2, fields.size());
		assertTrue("indexed fields were not read", fields.containsKey(FieldInfo.INDEXED));
		assertTrue("stored fields were not read", fields.containsKey(FieldInfo.STORED));

		HashSet<String> indexedFields = fields.get(FieldInfo.INDEXED);
		HashSet<String> storedFields = fields.get(FieldInfo.STORED);
		assertEquals("indexed fields set should have 2 entries", 2, indexedFields.size());
		assertEquals("stored fields set should have 2 entries", 2, storedFields.size());
		assertTrue("id should be in indexed fields", indexedFields.contains("id"));
		assertTrue("body should be in indexed fields", indexedFields.contains("body"));
		assertTrue("id should be in stored fields", storedFields.contains("id"));
		assertTrue("title should be in indexed fields", storedFields.contains("title"));
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongFile() throws Exception {
		FieldConfigFile fiFile = new FieldConfigFile(folder.concat("fields.wrong.read"), new FieldConfigCodec());
		HashMap<String, HashSet<String>> fields = (HashMap<String, HashSet<String>>)fiFile.read();
		assertEquals("when trying to read wrong formatted data, null should be returned", null, fields);
	}

	@Test
	public void testWrite() throws Exception {
		HashMap<String, HashSet<String>> fields = new HashMap<String, HashSet<String>>();
		String[] indexed = {"id", "body"};
		String[] stored = {"id", "title"};
		fields.put(FieldInfo.INDEXED, new HashSet<String>(Arrays.asList(indexed)));
		fields.put(FieldInfo.STORED, new HashSet<String>(Arrays.asList(stored)));

		String path = folder.concat("norms.test.write");
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		FieldConfigFile fiFile = new FieldConfigFile(path, new FieldConfigCodec());
		fiFile.write(fields);

		String textWritten = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		String textExpected = new String(Files.readAllBytes(Paths.get(folder.concat("fields.read"))), StandardCharsets.UTF_8);
		assertEquals("data was not written as expected", textExpected, textWritten);
	}
}
