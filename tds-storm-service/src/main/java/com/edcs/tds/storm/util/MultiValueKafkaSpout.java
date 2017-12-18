package com.edcs.tds.storm.util;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

import com.edcs.tds.storm.scheme.MessageScheme;

public class MultiValueKafkaSpout extends KafkaSpout {

	private static final long serialVersionUID = 1325451228364460167L;

	public MultiValueKafkaSpout(SpoutConfig spoutConf) {
		super(spoutConf);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(MessageScheme.SCHEME_MESSAGE_KEY, MessageScheme.SCHEME_MESSAGE_VALUE));
	}

}
