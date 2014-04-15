package com.vruiz.invertedindex.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Class used for logging. Just print the output in console
 */
public class Logger {

	private static Logger instance = null;

	/**
	 * use singleton to share the same instance among all classes
	 * @return
	 */
	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}


	public void error(String msg) {
		this.write("ERROR: ".concat(msg));
	}

	public void error(Exception e) {
		this.write("ERROR: ".concat(printStackTrace(e)));
	}

	public void error(String msg, Exception e) {
		this.write("ERROR: ".concat(msg).concat("\n").concat(printStackTrace(e)));
	}

	public void warn(String msg) {
		this.write("WARN: ".concat(msg));
	}

	public void info(String msg) {
		this.write("INFO: ".concat(msg));
	}

	private void write(String msg) {
		System.out.println(msg);
	}

	private String printStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
 }
