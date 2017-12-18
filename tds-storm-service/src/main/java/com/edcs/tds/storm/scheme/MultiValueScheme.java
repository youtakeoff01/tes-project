package com.edcs.tds.storm.scheme;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.storm.kafka.KeyValueScheme;
import org.apache.storm.kafka.KeyValueSchemeAsMultiScheme;
import org.apache.storm.tuple.Fields;

public class MultiValueScheme extends KeyValueSchemeAsMultiScheme {

	private static final long serialVersionUID = 1L;

	@Override
	public Iterable<List<Object>> deserializeKeyAndValue(ByteBuffer key, ByteBuffer value) {
		return super.deserializeKeyAndValue(key, value);
	}

	@Override
	public Fields getOutputFields() {
		return new Fields(MessageScheme.SCHEME_MESSAGE_KEY, MessageScheme.SCHEME_MESSAGE_VALUE);
	}

	public MultiValueScheme(KeyValueScheme scheme) {
		super(scheme);
	}

}
