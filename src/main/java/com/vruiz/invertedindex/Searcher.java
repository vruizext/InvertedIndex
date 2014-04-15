package com.vruiz.invertedindex;


import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Hit;
import com.vruiz.invertedindex.index.IndexReader;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import com.vruiz.invertedindex.util.Benchmark;
import com.vruiz.invertedindex.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Uses IndexReader to perform a search in the index
 */
public class Searcher {

	Logger log;

	IndexReader reader;


	public  Searcher() {
		this.log = new Logger();
	}

	/**
	 * open the index
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public IndexReader openIndexReader() throws IOException, CorruptIndexException {
		//resolve directory path
		String currentDirectory = new File("").getAbsolutePath();
		String directoryPath = currentDirectory.concat("/index/");
		System.out.printf("Reading index files from this directory: %s \n\n", directoryPath);

		this.reader = new IndexReader(new TxtFileDirectory(directoryPath));
		this.reader.open();
		return reader;
	}

	/**
	 * perform the search of one single term in the index
	 * @param term
	 * @return
	 */
	public TreeSet<Hit> search(String term) {
		TreeSet<Hit> resultSet = null;
		IndexReader reader = null;
		try {
			//search for term occurrences in body field
			resultSet = this.reader.search(Indexer.FieldName.BODY.toString(), term);
		} catch (IOException e) {
			this.log.error("There was an IO error reading the index files ", e);
		} catch (CorruptIndexException e) {
			this.log.error("Index data is corrupt ", e);
		} catch (IllegalArgumentException e) {
			Logger.getInstance().error(e.getMessage());
		} finally {
			//close open resources
			if (reader != null) {
				reader.close();
			}
		}
		return resultSet;
	}

	/**
	 * show the list of results
	 * @param hits
	 * @param term
	 */
	public String printHits(TreeSet<Hit> hits, String term) {
		if(hits == null || hits.isEmpty()) {
			return String.format("No documents found matching the term %s \n", term);
		}

		String out = String.format("%d Documents found matching the term %s: \n", hits.size(), term);

		Iterator it = hits.descendingSet().iterator();
		int i = 1;
		while(it.hasNext()) {
			Hit hit = (Hit) it.next();
			out = out.concat(String.format("%d - %f - %s \n", i++, hit.score(), hit.document().fields().get("title").data()));
		}

		return out;
	}


	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No query term specified!");
			System.exit(0);
		}

		Benchmark.getInstance().start("Searcher.main");
		try {
			Searcher searcher = new Searcher();
			searcher.openIndexReader();
			TreeSet<Hit> results = searcher.search(args[0]);
			System.out.println(searcher.printHits(results, args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		}
		Benchmark.getInstance().end("Searcher.main");

		long t = Benchmark.getInstance().getTime("Searcher.main");
		System.out.printf("\ntotal time for this query: %d milliseconds\n", t);
		long mem = Benchmark.getInstance().getMemory("Searcher.main");
		System.out.printf("memory used: %f MB\n", (float) mem / 1024 / 1024);
	}
}
