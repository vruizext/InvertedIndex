package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.index.CorruptIndexException;

import java.io.Writer;
import java.util.Formatter;
import java.util.Map;

/**
 * Provides an interface to encode and decode the index data that is read from and written to disk
 */
public interface Codec {


	/**
	 * encode a single entry
	 * @param entry a Map entry
	 * @return entry formatted to String
	 */
	public void writeEntry(Formatter formatter, Map.Entry entry) throws CorruptIndexException;

	public Map.Entry readEntry(String data) throws CorruptIndexException;

	/**
	 * To create custom Map.Entry objects, it's necessary to build an implementation of Map.Entry
	 * @param <K> key of the entry
	 * @param <V> value of the entry
	 */
	final class Entry<K,V> implements Map.Entry<K, V> {
		private final K key;
		private V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V v) {
			return this.value = v;
		}
	}

}
