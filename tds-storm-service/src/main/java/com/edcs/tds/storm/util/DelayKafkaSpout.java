package com.edcs.tds.storm.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;

public class DelayKafkaSpout extends KafkaSpout {

	private static final long serialVersionUID = 1325451228364460167L;

	public DelayKafkaSpout(SpoutConfig spoutConf) {
		super(spoutConf);
	}

	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
		try {
			TimeUnit.SECONDS.sleep(6);//spout并发发送1000条以后休息6秒
		} catch (InterruptedException e) {
		}
		super.open(conf, context, collector);
	}

}
