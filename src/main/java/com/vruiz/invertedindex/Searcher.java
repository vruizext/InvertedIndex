package com.vruiz.invertedindex;


import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Hit;
import com.vruiz.invertedindex.index.IndexReader;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import com.vruiz.invertedindex.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 */
public class Searcher {

	Logger log;


	public  Searcher() {
		this.log = new Logger();
	}

	public TreeSet<Hit> search(String term) {
		TreeSet<Hit> resultSet = null;
		IndexReader index = null;
		try {
			String directoryPath = new File("").getAbsolutePath().concat("index/");
			directoryPath = "/home/bik/IdeaProjects/MyInvertedIndex/index/";
			System.out.println(directoryPath);
			index = new IndexReader(new TxtFileDirectory(directoryPath));
			index.open();
			resultSet = index.search(Indexer.FieldName.BODY.toString(), term);
		} catch (IOException e) {
			this.log.error("There was an IO error reading the index files ", e);
		} catch (CorruptIndexException e) {
			this.log.error("Index data is corrupt ", e);
		} catch (Exception e) {
			this.log.error("An error occurred while searching: ", e);
		} finally {
			//close open resources
			if (index != null) {
				index.close();
			}
		}
		return resultSet;
	}

	public void printHits(TreeSet<Hit> hits, String term) {
		if(hits.isEmpty()) {
			System.out.printf("No documents found matching the term %s \n", term);
			System.exit(0);
		}

		System.out.printf("%d Documents found matching the term %s: \n", hits.size(), term);

		Iterator it = hits.descendingSet().iterator();
		int i = 1;
		while(it.hasNext()) {
			Hit hit = (Hit) it.next();
			System.out.printf("%d - %f - %s \n", i++, hit.score(), hit.document().fields().get("title").data());

		}
	}

	public static void main(String[] args) {
		long mem0 = Runtime.getRuntime().totalMemory();
		long t0 = Calendar.getInstance().getTimeInMillis();

		if (args.length == 0) {
			System.out.println("No query term specified!");
			System.exit(0);
		}

		Searcher searcher = new Searcher();

		TreeSet<Hit> results = searcher.search(args[0]);

		searcher.printHits(results, args[0]);

		long t = (Calendar.getInstance().getTimeInMillis() - t0);
		System.out.println("total time: " + t + " milliseconds");
		long mem = Runtime.getRuntime().totalMemory() - mem0;
		System.out.println("memory used: " + (float)mem/1024 + " MB");
	}
}
