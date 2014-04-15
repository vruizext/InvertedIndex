package com.vruiz.invertedindex.service;

import com.vruiz.invertedindex.Searcher;
import com.vruiz.invertedindex.index.CorruptIndexException;
import com.vruiz.invertedindex.index.Hit;
import com.vruiz.invertedindex.util.Benchmark;
import com.vruiz.invertedindex.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.TreeSet;

/**
 * Created by bik on 4/3/14.
 */
public class SearchServer {

	public static final int DEFAULT_PORT = 9095;

	public static final String END = "\t\t\t";

	private static Searcher searcher = null;

	public static Searcher getIndexSearcher() throws IOException, CorruptIndexException {
		if (SearchServer.searcher == null) {
			SearchServer.searcher  = new Searcher();
			SearchServer.searcher.openIndexReader();
			System.out.println("\nindex was opened\n");
		}
		return SearchServer.searcher;
	}

	private void handleQuery(Socket socket) throws IOException {

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter output = new PrintWriter(socket.getOutputStream(), false);

		String query = input.readLine();
		System.out.printf("\nnew query received: %s\n", query);
		if (query == null || query.length() == 0) {
			output.print("SERVER: empty query received!!! \n");
			output.flush();
			output.close();
			input.close();
			return;
		}
		Searcher indexSearch = null;
		try {
			indexSearch = SearchServer.getIndexSearcher();
			TreeSet<Hit> results = indexSearch.search(query);
			String out = indexSearch.printHits(results, query);
			output.print(out);
			output.flush();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			output.close();
			input.close();
		}
	}


	public static void main(String[] args) {

		int port = SearchServer.DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		ServerSocket serverSocket = null;
		SearchServer server = new SearchServer();
		try {
			//load the index before starting to receive queries
			SearchServer.getIndexSearcher();
			//open the server socket and listen to client connections
			serverSocket = new ServerSocket(port);
			System.out.printf("\nServer started at port %d. waiting for connections...\n", port);
			while(true) {
				Socket client = serverSocket.accept();
				Benchmark.getInstance().start("SearchServer");

				server.handleQuery(client);

				Benchmark.getInstance().end("SearchServer");
				long t = Benchmark.getInstance().getTime("SearchServer");
				System.out.printf("\ntotal time for this query: %d milliseconds\n", t);
				long mem = Benchmark.getInstance().getMemory("SearchServer");
				System.out.printf("memory used: %f MB\n", (float) mem / 1024 / 1024);
			}
		} catch (IOException e) {
			System.out.printf("there was some problem in the connection with the client %s \n", e.getMessage());
		} catch (CorruptIndexException e) {
			System.out.printf("index is corrupted and could not open it for search... %s \n", e.getMessage());
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}






	}
}
