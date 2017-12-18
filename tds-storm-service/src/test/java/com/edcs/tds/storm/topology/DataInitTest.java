package com.edcs.tds.storm.topology;

import java.util.Properties;

import com.edcs.tds.storm.model.MasterData;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class DataInitTest {
	private static final String TOPIC = "TES_Lee01";
	private static final String BROKER_LIST = "172.26.66.32:6667,172.26.66.33:6667,172.26.66.34:6667";

	public static void main(String[] args) throws Exception {
		/*Properties props = new Properties();
		props.put("bootstrap.servers", BROKER_LIST);
		props.put("producer.type", true);// 消息发送类型同步还是异步，默认为同步
		props.put("compression.codec", "gzip");// 消息的压缩格式
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");// 消息加密格式
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		for (int i = 1; i < 2001; i++) {
			String str = "{"
					+"\"remark\": \"MD_hhhhh\","
					+"\"sfc\": \"6454_DCR_25\","
					+"\"resourceId\": \"FXXX0001\","
					+"\"channelId\": 1,"
					+"\"sequenceId\": "+i+","
					+"\"cycle\": 2,"
					+"\"stepId\": 2,"
					+"\"stepName\": \"huacheng\","
					+"\"testTimeDuration\": 300,"
					+"\"timestamp\": \"2017-03-21 18:07:42\","
					+"\"svIcRange\": 5.0,"
					+"\"svIvRange\": 5.0,"
					+"\"pvVoltage\": 2.0,"
					+"\"pvCurrent\": 4.0,"
					+"\"pvIr\": 5.0,"
					+"\"pvTemperature\": 17.0,"
					+"\"pvChargeCapacity\": 5.0,"
					+"\"pvDischargeCapacity\": 5.0,"
					+"\"pvChargeEnergy\": 5.0,"
					+"\"pvDischargeEnergy\": 5.0,"
					+"\"pvDataFlag\": 1,"
					+"\"pvWorkType\": 2,"
					+"\"pvSubChannelData1\": {"
					+"\"sequenceId\": 4,"
					+"\"cycle\": 1,"
					+"\"stepId\": 1,"
					+"\"testTimeDuration\": 1,"
					+"\"voltage\": 1.0,"
					+"\"current\": 1.0,"
					+"\"ir\": 16.0,"
					+"\"temperature\": 17.0,"
					+"\"chargeCapacity\": 18.0,"
					+"\"dischargeCapacity\": 19.0,"
					+"\"chargeEnergy\": 20.0,"
					+"\"dischargeEnergy\": 21.0,"
					+"\"timestamp\": \"2017-03-21 18:07:43\","
					+"\"dataFlag\": 101,"
					+"\"workType\": 1"
					+"},"
					+"\"pvSubChannelData2\": {"
					+"\"sequenceId\": 4,"
					+"\"cycle\": 2,"
					+"\"stepId\": 1,"
					+"\"testTimeDuration\": 1,"
					+"\"voltage\": 1.0,"
					+"\"current\": 1.0,"
					+"\"ir\": 16.0,"
					+"\"temperature\": 17.0,"
					+"\"chargeCapacity\": 18.0,"
					+"\"dischargeCapacity\": 19.0,"
					+"\"chargeEnergy\": 20.0,"
					+"\"dischargeEnergy\": 21.0,"
					+"\"timestamp\": \"2017-03-21 18:07:43\","
					+"\"dataFlag\": 101,"
					+"\"workType\": 1"
					+"}}";
			producer.send(new ProducerRecord<String, String>(TOPIC,str));
			
			
		}
		System.out.println("end");
		producer.close();*/

		MasterData masterData = new MasterData();
		masterData.setHandle("gfgg");
		System.out.println(StringUtils.isNotBlank(masterData.getRemark()));
	}

	// public static void main(String[] args) {

	//
	// //TestingMessage testingMessage = DataInit.initRequestMessage();
	// //System.out.println(testingMessage.getCycle());
	// System.out.println("111111111");
	// }
	//

}
