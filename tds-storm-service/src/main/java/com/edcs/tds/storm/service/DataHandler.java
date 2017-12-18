package com.edcs.tds.storm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.edcs.tds.storm.cache.FilterMappingService;
import com.edcs.tds.storm.model.MDStepInfo;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingSubChannel;
import com.edcs.tds.storm.redis.RedisClient;
import com.edcs.tds.storm.scheme.MessageScheme;
import com.edcs.tds.storm.util.CommonUtil;
import com.edcs.tds.storm.util.JsonUtils;

import groovy.lang.Binding;

public class DataHandler {

	static final Logger logger = LoggerFactory.getLogger(DataHandler.class);

	/**
	 * 实现对测试数据（实时数据的初始化，将kafka拿出来的json测试数据转化为对应的java对象）
	 * 
	 * @param input
	 * @param redisClient
	 * @return
	 */
	public static TestingMessage initRequestMessage(Tuple input, DataService dataService, RedisClient redisClient)
			throws Exception {
		// 实现对Kafka测试数据的序列化；
		String data = input.getStringByField(MessageScheme.SCHEME_MESSAGE_VALUE);
		TestingMessage testingMsg = JsonUtils.toObject(data, TestingMessage.class);
		// 过滤工况测试数据
		FilterMappingService.initFilterMapping(redisClient);
		if (testingMsg != null && FilterMappingService.filterMessage(testingMsg.getResourceId())) {
			return null;
		}
		/*
		 * 如果测试数据的相对时间为0 但是 pvDataFlag值不是89，则取当前数据的上一条数据，看两条数据的工步id是否一样
		 * 如果不一样则说明这个状态有问题，需要改掉。
		 */
		if (testingMsg.isZeroTestTimeDuration() && !testingMsg.isStartStep()) {
			TestingMessage upTestingMsg = dataService.getUpTestingMsg(testingMsg, 1);
			if (upTestingMsg != null && upTestingMsg.getStepId() != testingMsg.getStepId()) {
				logger.warn("pvDataFlag is error,remark={},sequenceId={},pvDataFlag={}", testingMsg.getRemark(),
						testingMsg.getSequenceId(), testingMsg.getPvDataFlag());
				testingMsg.setPvDataFlag(89);
			}
		}
		// 设置测试数据的唯一id
		String msgId = CommonUtil.underlineLink().join(testingMsg.getRemark(), testingMsg.getResourceId(),
				testingMsg.getChannelId(), testingMsg.getStepId(), testingMsg.getSequenceId(),
				testingMsg.getTestTimeDuration(), testingMsg.getTimestamp());
		testingMsg.setMessageId(msgId);
		JSONObject jsonObject = JsonUtils.parseObject(data);
		int i = 0;// 来标注有多少子通道的。
		List<TestingSubChannel> lists = new ArrayList<TestingSubChannel>();// 用来存放子通道信息
		while (true) {
			i++;
			String str = jsonObject.getString(SystemConfig.SUBCHANNEL_NAME + i);
			if ("0".equals(str)) {
				continue;
			}
			JSONObject subJson = jsonObject.getJSONObject(SystemConfig.SUBCHANNEL_NAME + i);
			if (subJson == null) {
				break;
			} else {
				String strJson = JSONObject.toJSONString(subJson);// 获取子通道信息
				TestingSubChannel testingSubChannel = JsonUtils.toObject(strJson, TestingSubChannel.class);// 转化为子通道的对象
				testingSubChannel.setSubChannelName(SystemConfig.SUBCHANNEL_NAME + i);
				lists.add(testingSubChannel);
			}
		}
		testingMsg.setSubChannel(lists);

		return testingMsg;
	}

	/**
	 * 数据初始化
	 * 
	 * @param executeContext
	 * @param cacheService
	 * @param shellContext
	 * @param mDprocessInfo
	 * @return
	 */
	public static Binding initShellContext(TestingMessage testingMsg, DataService dataService, Binding shellContext,
			MasterData masterData) {
		// 将是否是一个公布的第一条测试数据和是否是最后一条测试数据的标志传递给规则配置中
		shellContext.setProperty(SystemConfig.DATASTATE, testingMsg.getPvDataFlag());
		/*
		 * 电流测试场景需要的参数
		 */
		BigDecimal pvcurrent = testingMsg.getPvCurrent();
		if (pvcurrent != null) {
			pvcurrent = pvcurrent.abs();
		}
		shellContext.setProperty(SystemConfig.PVCURRENT, pvcurrent);// 需要用来比较的电流
		shellContext.setProperty(SystemConfig.SVICRANGE, testingMsg.getSvIcRange());// 电流通道最大流程

		MDStepInfo mdStepInfo = masterData.getStepData().get(testingMsg.getStepId());// 获取这条测试数据对应的主数据的工步信息。

		// 恒压充电工步的 截止电流 （I截止）
		shellContext.setProperty(SystemConfig.SVSTEPENDCURRENT, mdStepInfo.getSvStepEndCurrent());
		// Ilast为工步最后一点电流值
		shellContext.setProperty(SystemConfig.SVCURRENT, mdStepInfo.getSvCurrent());
		// U截止为恒功率充电截止电压(U截止)
		shellContext.setProperty(SystemConfig.SVSTEPENDVOLTAGE, mdStepInfo.getSvStepEndVoltage());
		// （P恒冲） （P恒放）
		shellContext.setProperty(SystemConfig.SVPOWER, mdStepInfo.getSvPower());

		if (testingMsg.isStartStep()) {// 如果是工步的第一条数据则不用获取上一条测试数据
			// I i-1 为上一条数据的电流值
			shellContext.setProperty(SystemConfig.UPPVCURRENT, null);
			// U i-1 为上一条数据的电压值
			shellContext.setProperty(SystemConfig.UPPVVOLTAGE, null);
			// 上一条数据的相对时间
			shellContext.setProperty(SystemConfig.UPTESTTIMEDURATION, null);
		} else {
			// 获取上一条测试数据信息
			TestingMessage upTestingMsg = dataService.getUpTestingMsg(testingMsg, 1);
			if (upTestingMsg != null) {
				// I i-1为上一条数据的电流值
				shellContext.setProperty(SystemConfig.UPPVCURRENT, upTestingMsg.getPvCurrent());
				// Ui-1 为上一条数据的电压值
				shellContext.setProperty(SystemConfig.UPPVVOLTAGE, upTestingMsg.getPvVoltage());
				// 上一条数据的相对时间
				shellContext.setProperty(SystemConfig.UPTESTTIMEDURATION, upTestingMsg.getTestTimeDuration());
			}
		}
		/**
		 * 取上一个工步的stepName和svCurrent
         */
		Map<String, String> upStepInfoMap = dataService.getUpStepInfo(testingMsg,masterData);
		if (null != upStepInfoMap){
			shellContext.setProperty(SystemConfig.UPSTEPNAME,upStepInfoMap.get("stepName"));
			logger.info("upStepInfoMap.get(\"svCurrent\")"+upStepInfoMap.get("svCurrent"));
			shellContext.setProperty(SystemConfig.UPSTEPSVCURRENT, (StringUtils.isBlank(upStepInfoMap.get("svCurrent")) || "null".equals(upStepInfoMap.get("svCurrent"))) ? null : new BigDecimal(upStepInfoMap.get("svCurrent")));

		}
		// R恒 为流程设置恒阻值
		shellContext.setProperty(SystemConfig.CONSTANTIRVALUE, masterData.getConstantIrValue());

		/*
		 * 电压测试场景需要的参数
		 */
		shellContext.setProperty(SystemConfig.PVVOLTAGE, testingMsg.getPvVoltage());// 需要用来比较的电压
		shellContext.setProperty(SystemConfig.SVUPPERU, masterData.getSvUpperU());// U上限为测试流程中规定的上限电压
		shellContext.setProperty(SystemConfig.SVLOWERU, masterData.getSvLowerU());// U下限为测试流程中规定的上限电压

		if (SystemConfig.HOLD.equals(testingMsg.getStepName()) && testingMsg.getStepLogicNumber() == 1) {
			// 只有第一个搁置才会取
			TestingMessage firstTestingMsg = dataService.getFirstTestingMsg(testingMsg);
			if (firstTestingMsg != null) {
				shellContext.setProperty(SystemConfig.PVVOLTAGEFIRST, firstTestingMsg.getPvVoltage());// U1
			}
		}
		if (SystemConfig.HOLD.equals(testingMsg.getStepName()) && testingMsg.getStepLogicNumber() != 1) {
			// 搁置的时候才会有
			// 获取上一个工步的最后一条测试数据
			TestingMessage upStepTestingMsg = dataService.getUpStepTestingMsg(testingMsg, 1);
			if (upStepTestingMsg != null) {
				// U 放电末、U冲电末
				shellContext.setProperty(SystemConfig.UPSTEPPVVOLTAGE, upStepTestingMsg.getPvVoltage());
			}
		}
		// U恒压为恒压阶段设定电压值
		shellContext.setProperty(SystemConfig.SVVOLTAGE, mdStepInfo.getSvVoltage());
		if (testingMsg.getStepName().contains(SystemConfig.CHARGE)) {
			shellContext.setProperty(SystemConfig.PVCAPACITY, testingMsg.getPvChargeCapacity());// Ci为工步实时容量值
		}
		if (testingMsg.getStepName().contains(SystemConfig.DISCHARGE)) {
			shellContext.setProperty(SystemConfig.PVCAPACITY, testingMsg.getPvDischargeCapacity());// Ci为工步实时容量值
		}
		// C设定 为工步设定截止容量
		shellContext.setProperty(SystemConfig.SVSTEPENDCAPACITY, mdStepInfo.getSvStepEndCapacity());
		// C为电芯标称容量
		shellContext.setProperty(SystemConfig.SVCAPACITYVALUE, masterData.getSvCapacityValue());
		/**
		 * 相对时间
		 */
		shellContext.setProperty(SystemConfig.TESTTIMEDURATION, testingMsg.getTestTimeDuration());// 需要用来比较的相对时间
		shellContext.setProperty(SystemConfig.SVTIME, mdStepInfo.getSvTime());// 工步的相对时间
		shellContext.setProperty(SystemConfig.TIME_CONTANT_P, testingMsg.getTimeConstantP());// t恒压
		return shellContext;
	}
}