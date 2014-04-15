package com.vruiz.invertedindex.store.file;

import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.store.codec.NormsCodec;
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
public class NormsFileTest {

	String folder = "testfiles/";
	@Test
	public void testRead() throws Exception {
		NormsFile fNorms = new NormsFile(folder.concat("norms.test.read"), new NormsCodec());
		HashMap<Long,Integer> normsRead = (HashMap<Long,Integer>)fNorms.read();
		assertEquals("there should be three entries loaded in the norms map", 2, normsRead.size());
		assertTrue("key 1 not read", normsRead.containsKey((long)1));
		assertTrue("key 2 not read", normsRead.containsKey((long)2));
		assertEquals("wrong value for key 1 ", 5, (int) normsRead.get((long)1));
		assertEquals("wrong value for key 2", 10, (int)normsRead.get((long)2));
	}

	@Test(expected = CorruptIndexException.class)
	public void testReadWrongFile() throws Exception {
		NormsFile fNorms = new NormsFile(folder.concat("stored.test.read"), new NormsCodec());
		HashMap<Long,Integer> normsRead = (HashMap<Long,Integer>)fNorms.read();
		assertEquals("when trying to read wrong formatted data, null should be returned", null, normsRead);
	}

	@Test
	public void testWrite() throws Exception {
		HashMap<Long,Integer> norms = new HashMap<Long, Integer>();
		norms.put((long)1, 5);
		norms.put((long)2,10);
		String path = folder.concat("norms.test.write");
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		NormsFile nFile = new NormsFile(path, new NormsCodec());
		nFile.write(norms);

		String textWritten = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		String textExpected = new String(Files.readAllBytes(Paths.get(folder.concat("norms.test.read"))), StandardCharsets.UTF_8);
		assertEquals("data was not written as expected", textExpected, textWritten);
	}
}
