package com.vruiz.invertedindex;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.*;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import com.vruiz.invertedindex.util.Logger;

import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Provides a user interface to index files
 * Needs file name as first and only argument
 */
public final class Indexer {

	Logger log;

	protected String directoryPath;

	public  Indexer() {
		this.log = new Logger();
	}

	/**
	 * this enum is used to reference  the names of the fields, Helps to avoid typo errors 8)
	 * TODO in a real production environment it would be better to allow the user configure field names and options
	 * TODO using a config file
	 */
	public static enum FieldName {
		ID("id"), TITLE("title"), BODY("body");

		private String name;

		private FieldName(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}


	/**
	 * Just get data and return new Document
	 * @param id article id
	 * @param title article title
	 * @param body article body
	 * @return Document object populated with the given id, title and body
	 */
	private Document createDocument(String id, String title, String body) {
		//let's create a document and add it to the index
		Document doc = new Document();
		//create fields and add them to the do
		//field only needs to be stored
		Field field = new Field(FieldName.TITLE.toString(), title, new FieldInfo(false, true));
		doc.addField(field);
		//body needs to be indexed and tokenized
		field = new Field(FieldName.BODY.toString(), body, new FieldInfo(true, false, Tokenizer.class));
		doc.addField(field);

		// article id is not required to be in the index, even it could be indexed, I take it out to
		// improve performance and reduce memory and storage usage.
		/*
		field = new Field(FieldName.ID.toString(), id, new FieldInfo(true, true));
		doc.addField(field);
		*/

		return doc;
	}

	/**
	 * get a line from TSV file and returns a Document with the article data
	 * @param line a line read from a tsv file, ie, fields separated by tabs
	 */
	private Document documentFromString(String line) {
		//parse article fields, tab separated values
		String [] fields = line.split("\t");
		//if length is other than 3, that means something is wrong with this data, dont trust it
		if (fields.length != 3) {
			this.log.warn(String.format("wrong line: %s ", line.substring(0, 50)));
		}
		String id = fields[0]; //first is  article id
		String title = fields[1]; // after id we have the title
		String body = fields[2]; // at position 3 we have article body
		//validate that fields are not empty,
		// id is actually optional, since it's not required to be stored in the index
		if (title.isEmpty() || body.isEmpty()) {
			this.log.warn(String.format("wrong article: %s - %s - %s", id, title, body.substring(0, 25)));
			return null;
		}
		return createDocument(id, title, body);
	}

	/**
	 * read the input file, one document per line, and add documents to the index
	 * @param file a file referencing the file being indexed
	 * @throws IOException
	 */
	private void indexFile(File file) throws IOException {
		//the way the index is stored in disk is managed by Directory. TxtFileDirectory is a naive approach saving
		// data in text format. Custom implementations can be set here
		String filePath = file.getAbsolutePath();
		directoryPath = filePath.replaceAll(file.getName(), "").concat("index/");
		System.out.println("directory path: " + directoryPath);
		IndexWriter indexer = new IndexWriter(new TxtFileDirectory(directoryPath));
		try {
			//delete old files before start indexing
			indexer.reset();
		} catch (IOException e) {
			this.log.error("There was an IO error deleting the index files ", e);
		} catch (CorruptIndexException e) {
			this.log.error("Index data is corrupted, please delete files manually and try again ", e);
		} catch (Exception e) {
			this.log.error(directoryPath, e);
		}

		FileReader fr = new FileReader(filePath);
		BufferedReader reader = new BufferedReader(fr);

		String line;
		while ((line = reader.readLine()) != null) {
			Document doc = documentFromString(line);
			if (doc == null) {
				continue;
			}
			indexer.addDocument(doc);
		}

		try {
			//write index to disk
			indexer.flush();
		} catch (IOException e) {
			this.log.error("There was an IO error writing the index to disk ", e);
		} catch (CorruptIndexException e) {
			this.log.error("Index corrupted ", e);
		} catch (Exception e) {
			this.log.error(e);
		} finally {
			//close open resources
			indexer.close();
		}

		this.log.info(indexer.getNumDocs() + " documents indexed, containing " + indexer.getNumTerms() + " different terms");
	}

	public static void main(String[] args){
		System.out.println(new File("").getAbsolutePath());

		long mem0 = Runtime.getRuntime().totalMemory();
		long t0 = Calendar.getInstance().getTimeInMillis();

		if (args.length == 0) {
			System.out.println("no tsv file specified");
			System.exit(0);
		}

		String fileName = args[0];
		File f = new File(fileName);
		if (!f.exists()) {
			System.out.println("tsv file can't be read, is the file there? " + fileName);
			System.exit(0);
		}

		Indexer indexer = new Indexer();

		try {
			indexer.indexFile(f);
		} catch (IOException e) {
			System.out.println("There was a problem reading the TSV file " );
		}

		long t = (Calendar.getInstance().getTimeInMillis() - t0);
		System.out.println("total time: " + t + " milliseconds");
		long mem = Runtime.getRuntime().totalMemory() - mem0;
		System.out.println("memory used: " + (float)mem/1024 + " MB");
		String filePath = f.getAbsolutePath();
		filePath = filePath.replaceAll(f.getName(), "").concat("index/");
		IndexReader ir = new IndexReader(new TxtFileDirectory(filePath));

		TreeSet<Hit> hits = null;
		try {
			hits = ir.search("body", "hello");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		}

		if (hits.isEmpty()) {
			Iterator it = hits.descendingSet().iterator();
			int i = 1;
			while(it.hasNext()) {
				Hit hit = (Hit) it.next();
				System.out.printf("%d - %f - %s \n", i, hit.score(), hit.document().fields().get("title").data());
				i++;

			}
		}

		t = (Calendar.getInstance().getTimeInMillis() - t);
		System.out.println("time for search: " + t + " milliseconds");
	}
}
