package com.edcs.tds.storm.topology.calc;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import com.edcs.tds.storm.scheme.MessageScheme;
import com.edcs.tds.storm.topology.BaseTopology;
import com.edcs.tds.storm.util.DelayKafkaSpout;

public class CalcTopology extends BaseTopology {

	private int spoutNum = 1;
	private int calcNum = 1;

//	static final String[] TOPIC_DEF = { "TES_TEST01", "TES_TEST02"};
	
	static final String[] TOPIC_DEF = { "TES_PRODUCT01", "TES_PRODUCT02", "TES_PRODUCT03", "TES_PRODUCT04", "TES_PRODUCT05",
			"TES_PRODUCT06", "TES_PRODUCT07", "TES_PRODUCT08" };

	@Override
	public String getTopologyName() {
		return "tds-calc";
	}

	@Override
	public String getConfigName() {
		return "tds-calc-topology.xml";
	}

	@Override
	public int getWorkerNumber() {
		return 1;
	}

	@Override
	public void addOption(Options options) {
		options.addOption("spout", true, "spoutParallelism");
		options.addOption("calc", true, "calcParallelism");
	}

	@Override
	public void setupOptionValue(CommandLine cmd) {
		spoutNum = Integer.parseInt(cmd.getOptionValue("spout", "1").trim());
		calcNum = Integer.parseInt(cmd.getOptionValue("calc", "1").trim());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createTopology(TopologyBuilder builder) {
		// init all unit
		for (int i = 0; i < TOPIC_DEF.length; i++) {
			KafkaSpout kafkaSpout = new DelayKafkaSpout(getSpoutConfig(TOPIC_DEF[i]));
			String spoutId = StringUtils.join("kafkaSpout", (i + 1));
			builder.setSpout(spoutId, kafkaSpout, spoutNum);
			CalcBolt bolt = new CalcBolt();
			builder.setBolt(StringUtils.join("CalcBolt", (i + 1)), bolt, calcNum).fieldsGrouping(spoutId,
					new Fields(MessageScheme.SCHEME_MESSAGE_KEY));
		}
	}

	public static void main(String[] args) throws Exception {
		CalcTopology topology = new CalcTopology();
		topology.run(args);
	}

}
