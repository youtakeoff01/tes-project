package com.edcs.tds.storm.topology;

import java.io.IOException;
import java.util.Properties;
import java.lang.Process;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class TestCre {
	private static final String TOPIC = "TES_test02";
	private static final String BROKER_LIST = "172.26.38.71:6667,172.26.38.72:6667,172.26.38.73:6667";



	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", BROKER_LIST);
		props.put("producer.type", true);// 消息发送类型同步还是异步，默认为同步
		props.put("compression.codec", "gzip");// 消息的压缩格式
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");// 消息加密格式
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		for (int i = 11140; i < 11143; i++) {
			System.out.println("进入");
			for (int j = 0; j < 2000; j++) {
				Producer<String, String> producer = new KafkaProducer<String, String>(props);
				String test = "{" + "\"remark\": \"T3-20161017-1597-432581_TES-Cycle_3_C0F100_73337_TESE3_"+i+"\"," + "\"sfc\": \"06168D001001\","
						+ "\"resourceId\": \"FXL102914\"," + "\"channelId\": 8," + "\"sequenceId\": " + j + ","
						+ "\"cycle\": 5," + "\"stepId\": 6," + "\"stepName\": \"搁置\"," + "\"testTimeDuration\": 0.0,"
						+ "\"timeStamp\": \"2017-08-14 11:51:22\"," + "\"svIcRange\": 6.0," + "\"svIvRange\": 5.0,"
						+ "\"pvVoltage\": 10," + "\"pvCurrent\": 4.8," + "\"pvIr\": 5.7,"
						+ "\"pvTemperature\": 8.80," + "\"pvChargeCapacity\": 89," + "\"pvDischargeCapacity\": 67,"
						+ "\"pvChargeEnergy\": 7.00," + "\"pvDischargeEnergy\": 3.89," + "\"pvDataFlag\": " + 85 + ","
						+ "\"pvWorkType\": 1," + "\"pvSubChannelData1\": {" + "\"sequenceId\": 1," + "\"cycle\": 0,"
						+ "\"stepId\": 1," + "\"stepName\": \"搁置\"," + "\"testTimeDuration\": 0.0,"
						+ "\"voltage\": 4.0568," + "\"current\": 0.0," + "\"ir\": 0.0," + "\"temperature\": 44.7,"
						+ "\"chargeCapacity\": 0.0," + "\"dischargeCapacity\": 0.0," + "\"chargeEnergy\": 0.0,"
						+ "\"dischargeEnergy\": 0.0," + "\"timestamp\": \"2017-08-14 11:51:22\"," + "\"dataFlag\": 101,"
						+ "\"workType\": 1" + "}," + "\"pvSubChannelData2\": 0," + "\"pvSubChannelData3\": 0,"
						+ "\"pvSubChannelData4\": 0," + "\"pvSubChannelData5\": 0," + "\"pvSubChannelData6\": 0" + "}"+"\n";
				producer.send(new ProducerRecord(TOPIC, "T3-20161017-1597-432581_TES-Cycle_3_C0F100_73337", test));
				producer.close();
			}
		}
		new Thread().destroy();
		System.out.println("11111");
	}

}
