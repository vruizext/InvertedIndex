package com.vruiz.invertedindex.store.codec;

import com.vruiz.invertedindex.index.CorruptIndexException;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Serialize to String FieldInfo data
 * Format used to codified the data in this file is:
 * {fieldOption1}:{fieldName1},{fieldName2},...\n
 * {fieldOption2}:{fieldName1},{field2},{fieldName3},...\n
 * ...
 *
 * currently only fieldTypes 'indexed' and 'stored' are implemented
 */
public class FieldConfigCodec implements Codec{

	private final static String FORMAT_STRING_1 = "%s:";
	private final static String FORMAT_STRING_2 = "%s,";

	protected static final Pattern SEPARATOR_1 = Pattern.compile(":");
	protected static final Pattern SEPARATOR_2 = Pattern.compile(",");

	@Override
	public void writeEntry(Formatter formatter, Map.Entry entry) throws CorruptIndexException {
		Object key = entry.getKey();
		Object val = entry.getValue();

		if (key == null || val == null) {
			throw new CorruptIndexException("corrupted data entry");
		}
		//key is fieldType; indexed, stored..
		String fieldType = (String)key;
		//values are fields for a fieldType: title, body, etc...
		Set<String> fieldNames = (Set<String>)val;
		formatter.format(FORMAT_STRING_1, fieldType);
		for(String name: fieldNames) {
			if (name == null || name.length() == 0) {
				throw new CorruptIndexException("corrupted data in  entry");
			}
			formatter.format(FORMAT_STRING_2, name);
		}
		formatter.format("\n");
	}

	@Override
	public Map.Entry readEntry(String data) throws CorruptIndexException {
		//split on ":" to get documentId and count
		String[] parts = SEPARATOR_1.split(data);
		if (parts.length != 2) {
			throw new CorruptIndexException("wrong data format: ".concat(data));
		}
		String fieldType = parts[0];
		if (fieldType.length() == 0) {
			throw new CorruptIndexException("wrong data format: ".concat(data));
		}
		String[] names = SEPARATOR_2.split(parts[1]);
		HashSet<String> fieldNames = new HashSet<>();
		for(String fieldName: names) {
			if(fieldName.length() == 0) {
				throw new CorruptIndexException("wrong data format: ".concat(data));
			}
			fieldNames.add(fieldName);
		}


		return new Codec.Entry<>(fieldType, fieldNames);
	}
}