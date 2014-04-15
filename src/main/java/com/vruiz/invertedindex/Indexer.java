package com.vruiz.invertedindex;

import com.vruiz.invertedindex.document.Document;
import com.vruiz.invertedindex.document.Field;
import com.vruiz.invertedindex.document.FieldInfo;
import com.vruiz.invertedindex.index.*;
import com.vruiz.invertedindex.parse.TextParser;
import com.vruiz.invertedindex.store.TxtFileDirectory;
import com.vruiz.invertedindex.util.Benchmark;
import com.vruiz.invertedindex.util.Logger;

import java.io.*;
import java.util.Calendar;

/**
 * Provides a user interface to index files
 * Needs file name as first and only argument
 */
public final class Indexer {

	Logger log;

	protected String directoryPath;

	public  Indexer(String path) {
		this.directoryPath = path;
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
		field = new Field(FieldName.BODY.toString(), body, new FieldInfo(true, false, TextParser.class));
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
			return null;
		}
		String id = fields[0]; //first is  article id
		String title = fields[1]; // after id we have the title
		String body = fields[2]; // at position 3 we have article body
		//validate that fields are not empty,
		// id is actually optional, since it's not required to be stored in the index
		if (title.isEmpty() || body.isEmpty()) {
			this.log.warn(String.format("wrong article, some data missing: %s - %s - %s", id, title, body.substring(0, 25)));
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

		IndexWriter indexer = new IndexWriter(new TxtFileDirectory(this.directoryPath));
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
		BufferedReader reader = new BufferedReader(fr, 65536);

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

		this.log.info(String.format("%d documents indexed, %d different terms", indexer.getNumDocs(), indexer.getNumTerms()));
	}

	public static void main(String[] args){
		String currentDirectory = new File("").getAbsolutePath();

		if (args.length == 0) {
			System.out.println("no tsv file specified");
			System.exit(0);
		}

		String fileName = args[0];
		File f = new File(fileName);
		if (!f.exists()) {
			System.out.printf("tsv file can't be read, is the file there? %s\n", fileName);
			System.exit(0);
		}

		String directoryPath = currentDirectory.concat("/index/");
		System.out.printf("\nBuilding index in path: %s \n", directoryPath);

		Benchmark.getInstance().start("Indexer.main");
		try {
			Indexer indexer = new Indexer(directoryPath);
			indexer.indexFile(f);
		} catch (IOException e) {
			System.out.println("There was a problem reading the TSV file " );
		}

		Benchmark.getInstance().end("Indexer.main");

		long t = Benchmark.getInstance().getTime("Indexer.main");
		System.out.printf("\ntotal time for indexing: %d milliseconds\n", t);
		long mem = Benchmark.getInstance().getMemory("Indexer.main");
		System.out.printf("memory used: %f MB\n", (float) mem / 1024 / 1024);

		t = Benchmark.getInstance().getTime("IndexWriter.flush");
		System.out.printf("\ntime in IndexWriter.flush : %d milliseconds\n", t);
	}
}
