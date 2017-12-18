package com.edcs.tds.storm.scheme;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.storm.kafka.KeyValueScheme;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.util.JsonUtils;

public class MessageGroupScheme implements KeyValueScheme {

//	private static final Logger logger = LoggerFactory.getLogger(MessageGroupScheme.class);

	private static final long serialVersionUID = 1L;

	public static final String SCHEME_MESSAGE_KEY = "remark";
	public static final String SCHEME_MESSAGE_VALUE = "message";

	@Override
	public Fields getOutputFields() {
		return new Fields(SCHEME_MESSAGE_KEY, SCHEME_MESSAGE_VALUE);
	}

	@Override
	public List<Object> deserializeKeyAndValue(ByteBuffer key, ByteBuffer value) {
		String strKey = null;
		String strValue = StringScheme.deserializeString(value);
		if (key == null) {
			try {
				TestingMessage testingMsg = JsonUtils.toObject(strValue, TestingMessage.class);
				strKey = testingMsg.getRemark();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			strKey = StringScheme.deserializeString(key);
		}
		return new Values(strKey, strValue);
	}

	@Override
	public List<Object> deserialize(ByteBuffer value) {
		return deserializeKeyAndValue(null, value);
	}

}
