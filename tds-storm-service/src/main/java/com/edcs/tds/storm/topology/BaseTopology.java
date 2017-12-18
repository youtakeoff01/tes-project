package com.edcs.tds.storm.topology;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.metric.LoggingMetricsConsumer;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.joda.time.DateTime;

import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingResultData;
import com.edcs.tds.storm.model.TestingSubChannel;
import com.edcs.tds.storm.scheme.MessageGroupScheme;
import com.edcs.tds.storm.util.StormBeanFactory;
import com.google.common.base.Preconditions;

@SuppressWarnings({ "unchecked" })
public abstract class BaseTopology {

	private Config config = new Config();

	private Options options = new Options();

	private StormBeanFactory stormBeanFactory;

	public abstract String getTopologyName();

	public abstract String getConfigName();

	public abstract int getWorkerNumber();

	public abstract void addOption(Options options);

	public abstract void setupOptionValue(CommandLine cmd);

	public abstract void createTopology(TopologyBuilder builder);

	public BaseTopology() {
		config.setFallBackOnJavaSerialization(false);
		config.setSkipMissingKryoRegistrations(false);
		config.registerSerialization(Date.class);
		config.registerSerialization(HashMap.class);
		config.registerSerialization(Map.class);
		config.registerSerialization(TestingResultData.class);
		config.registerSerialization(TestingMessage.class);
		config.registerSerialization(TestingSubChannel.class);
		config.registerSerialization(BigDecimal.class);
		config.registerSerialization(Timestamp.class);
		config.registerMetricsConsumer(LoggingMetricsConsumer.class, 1);

		options.addOption("name", true, "Topology name");
		options.addOption("conf", false, "Config xml");
		options.addOption("workers", true, "Topology wokers");
		addOption(options);
	}

	public void setupConfig(CommandLine cmd) {
		String confLocation = cmd.getOptionValue("conf", getConfigName());
		stormBeanFactory = new StormBeanFactory(confLocation);
		Map<String, Object> stormConfig = stormBeanFactory.getBean("stormConfig", Map.class);
		Preconditions.checkNotNull(stormConfig);
		config.putAll(stormConfig);
		config.put(StormBeanFactory.SPRING_BEAN_FACTORY_XML, stormBeanFactory.getXml());

		String numWorkers = cmd.getOptionValue("workers");
		if (numWorkers != null) {
			config.setNumWorkers(Integer.parseInt(numWorkers));
		} else {
			config.setNumWorkers(getWorkerNumber());
		}
		config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 1000);//专治failed
		config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 950);
		config.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 16384);
		config.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 16384);
		config.put(Config.TOPOLOGY_ACKER_EXECUTORS, config.get(Config.TOPOLOGY_WORKERS));
	}

	protected KafkaSpout getKafkaSpout(String topic) {
		SpoutConfig spoutConfig = getSpoutConfig(topic);
		return new KafkaSpout(spoutConfig);
	}

	public SpoutConfig getSpoutConfig(String topic) {
		String brokerZkStr = (String) config.get("kafka.brokerZkStr");
		String brokerZkPath = (String) config.get("kafka.brokerZkPath");

		List<String> zkServers = (List<String>) config.get("kafka.offset.zkServers");
		Integer zkPort = Integer.parseInt(String.valueOf(config.get("kafka.offset.zkPort")));
		String zkRoot = (String) config.get("kafka.offset.zkRoot");
		String id = StringUtils.join(getTopologyName(), "-", topic);

		BrokerHosts kafkaBrokerZk = new ZkHosts(brokerZkStr, brokerZkPath);
		SpoutConfig spoutConfig = new SpoutConfig(kafkaBrokerZk, topic, zkRoot, id);
		spoutConfig.zkServers = zkServers;
		spoutConfig.zkPort = zkPort;
		spoutConfig.zkRoot = zkRoot;
		spoutConfig.stateUpdateIntervalMs = 30000;
		// spoutConfig.scheme = new MultiValueScheme(new
		// StringKeyValueScheme());
		spoutConfig.scheme = new SchemeAsMultiScheme(new MessageGroupScheme());
		return spoutConfig;
	}

	public void runForLocal(String[] args) throws Exception {

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("topology", options);
		setupConfig(cmd);
		//
		config.setDebug(true);
		config.setNumWorkers(1);

		TopologyBuilder builder = new TopologyBuilder();
		createTopology(builder);
		LocalCluster cluster = new LocalCluster();
		String topoName = cmd.getOptionValue("name",
				StringUtils.join(getTopologyName(), "-", new DateTime().toString("yyyyMMdd-HHmmss")));
		cluster.submitTopology(topoName, config, builder.createTopology());
	}

	public void run(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("topology", options);
		setupConfig(cmd);
		setupOptionValue(cmd);
		TopologyBuilder builder = new TopologyBuilder();
		createTopology(builder);
		String topoName = cmd.getOptionValue("name",
				StringUtils.join(getTopologyName(), "-", new DateTime().toString("yyyyMMdd-HHmmss")));
		StormSubmitter.submitTopologyWithProgressBar(topoName, config, builder.createTopology());
	}
}
