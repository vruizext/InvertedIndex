package com.vruiz.invertedindex.service;

import com.vruiz.invertedindex.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by bik on 4/3/14.
 */
public class SearchServer {

	private static class Searcher extends Thread {
		private Socket socket;
		private int clientId;

		public Searcher(Socket socket, int clientId) {
			this.socket = socket;
			this.clientId = clientId;
			(Logger.getInstance()).info(String.format("connection received from client %d at %s", clientId, socket));
		}
	}
}
