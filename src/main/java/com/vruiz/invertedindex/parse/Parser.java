package com.vruiz.invertedindex.parse;

import java.util.HashMap;

/**
 * A Parser defines DataStreams, which are used to parse and clean the field data (text,..) before it's indexed
 * Actually, this class define a set of rules to extract the terms from the source text. Each of these is applied by
 * a component, subclass of DataStream. This rules are defined in createStreamChain. Classes implementing a
 * Parser  must override createStreamChain with their custom rules ie components.
 *
 * To improve efficiency, components are created only once, and streamChainPerFields holds then the reference to
 * the StreamChain defined for every field
 */
public abstract class Parser {

	public static HashMap<String, StreamChain> streamChainPerField = new HashMap<>();

	public abstract StreamChain createStreamChain(final String fieldName);

	public final DataStream dataStream(String fieldName, String text) {
		StreamChain stream = streamChainPerField.get(fieldName);
		if (stream == null) {
			stream = createStreamChain(fieldName);
			streamChainPerField.put(fieldName, stream);
		}
		stream.setData(text);
		return stream.getDataStream();
	}


	/**
	 * This class encapsulates the outer components of a DataStream. It provides
	 * access to the source ({@link Tokenizer}) and the outer end, an
	 * instance of {@link DataStream} which is the object returned by DataParser.dataStream
	 */
	public static class StreamChain {

		/**
		 * source of the tokens, has to be {@link Tokenizer} or subclass of it
		 */
		protected final Tokenizer sourceStream;

		/**
		 * this is the outer component decorating the chain.
		 * if there are no filters defined, it will be the same as the source
		 */
		protected final DataStream endStream;

		public StreamChain(final Tokenizer source, final DataStream end) {
			this.sourceStream = source;
			this.endStream = end;
		}

		public void setData(final String input) {
			sourceStream.setData(input);
		}

		/**
		 * the last component in the chain
		 * @return the last component {@link DataStream}
		 */
		public DataStream getDataStream() {
			return endStream;
		}

	}
}
