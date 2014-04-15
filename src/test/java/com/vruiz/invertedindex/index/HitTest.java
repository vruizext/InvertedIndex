package com.vruiz.invertedindex.index;

import static org.junit.Assert.*;

import com.vruiz.invertedindex.document.Document;
import org.junit.Test;

import java.util.*;

/**
 * HitTest
 */
public class HitTest {
	@Test
	public void testCompareTo() throws Exception {
		TreeSet<Hit> resultSet = new TreeSet<>();
		Document d1 = new Document(1);
		Document d2 = new Document(2);
		Document d3 = new Document(3);
		resultSet.add(new Hit(d1, 1));
		resultSet.add(new Hit(d2, 2));
		resultSet.add(new Hit(d3, 0.5f));

		Iterator it = resultSet.descendingSet().iterator();
		Hit h = (Hit)it.next();
		assertEquals("first is doc 2", d2.getDocumentId(), h.document().getDocumentId());
		h = (Hit)it.next();
		assertEquals("second is doc 1", d1.getDocumentId(), h.document().getDocumentId());
		h = (Hit)it.next();
		assertEquals("last is doc 3", d3.getDocumentId(), h.document().getDocumentId());
	}
}
