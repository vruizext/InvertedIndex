package com.vruiz.invertedindex.util;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Class used to track time spent by different modules of the application, as well as memory used
 */
public class Benchmark {

	private static Benchmark instance = null;

	private HashMap<String, Long> timers;
	private HashMap<String, Long> memory;

	private Benchmark() {
		timers = new HashMap<>();
		memory = new HashMap<>();
	}

	public static Benchmark getInstance() {
		if (instance == null) {
			instance = new Benchmark();
		}
		return instance;
	}

	public void start(String name) {
		long t0 = Calendar.getInstance().getTimeInMillis();
		long mem0 = Runtime.getRuntime().totalMemory();
		timers.put(name, t0);
		memory.put(name, mem0);

	}

	public void end(String name) {
		long t = timers.get(name);
		long mem = memory.get(name);

		timers.put(name, (Calendar.getInstance().getTimeInMillis() - t));
		memory.put(name, Runtime.getRuntime().totalMemory() - mem);
	}

	public long getTime(String name) {
		return timers.get(name);
	}

	public long getMemory(String name) {
		return memory.get(name);
	}
}
