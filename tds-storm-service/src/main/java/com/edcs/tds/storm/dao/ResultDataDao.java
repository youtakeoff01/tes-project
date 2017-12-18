package com.edcs.tds.storm.dao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.dao.domain.AlertInfoModel;
import com.edcs.tds.storm.dao.domain.AlertListInfoModel;
import com.edcs.tds.storm.dao.domain.OriginalDataModel;
import com.edcs.tds.storm.dao.domain.SubChannelModel;
import com.edcs.tds.storm.dao.domain.ZipDataModel;
import com.edcs.tds.storm.dao.impl.AlertListInfoImpl;
import com.edcs.tds.storm.db.HanaDataHandler;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingResultData;
import com.edcs.tds.storm.model.TestingSubChannel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class ResultDataDao {
	private static final Logger logger = LoggerFactory.getLogger(ResultDataDao.class);
	private AlertListInfoImpl alertListInfoImpl;
	private HanaDataHandler hanaDataHandler;

	public void setAlertListInfoImpl(AlertListInfoImpl alertListInfoImpl) {
		this.alertListInfoImpl = alertListInfoImpl;
	}

	public void setHanaDataHandler(HanaDataHandler hanaDataHandler) {
		this.hanaDataHandler = hanaDataHandler;
	}

	/**
	 * @paramresultDatas
	 */
	public void setAllData(ConcurrentMap<String, List<TestingResultData>> maps) {
		List<AlertListInfoModel> listInfo = Lists.newArrayList();
		List<AlertInfoModel> info = Lists.newArrayList();
		List<SubChannelModel> subInfo = Lists.newArrayList();
		List<OriginalDataModel> origInfo = Lists.newArrayList();
		List<ZipDataModel> zipDataInfo = null;
		List<TestingResultData> testingDataInfo = null;

		for (Map.Entry<String, List<TestingResultData>> entry : maps.entrySet()) {
			List<TestingResultData> testingDatas = entry.getValue();

			TestingResultData testingData = testingDatas.get(0);

			Map<String, String> isCateAlertmap = Maps.newHashMap();
			isCateAlertmap.put("curr", "false");
			isCateAlertmap.put("volt", "false");
			isCateAlertmap.put("capa", "false");
			isCateAlertmap.put("time", "false");
			isCateAlertmap.put("temp", "false");
			isCateAlertmap.put("processDataAlert", "false");
			try {
				if (testingData.getAlertLevel() > 0) {
					AlertListInfoModel alertListInfoModel = hanaDataHandler.queryAlterInfoNumber(testingData, listInfo);
					if (alertListInfoModel != null) {
						String alertListInfohandle = alertListInfoModel.getAlertListInfohandle();
						isCateAlertmap = getAlertInfo(testingDatas, alertListInfohandle, info);
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			// 获取原始数据信息
			getOrignalData(testingData, isCateAlertmap, subInfo, origInfo);
			try {
				// 更新主数据状态
				if (testingData.getTestingMessage().isFirstMsg()) {
					hanaDataHandler.modifiedStatus(testingData, 1);
				}
				if (testingData.getTestingMessage().isProtectMsg()) {
					hanaDataHandler.modifiedStatus(testingData, 3);
				}
				if (testingData.getTestingMessage().isOverMsg()) {
					hanaDataHandler.modifiedStatus(testingData, 0);
				}
			} catch (Exception e) {
				logger.error("update mainData error,remark:{}", testingData.getTestingMessage().getRemark(), e);
			}
			if (testingData.getTestingMessage().isOverStep() && testingData.getIsContainMainData().equals("1")) {
				try {
					if(zipDataInfo == null){
						zipDataInfo = Lists.newArrayList();
					}
					setZipData(testingData, zipDataInfo);
				} catch (Exception e) {
					logger.error("setZipData error {},remark:{},zipDatainfo:{} ", e, testingData.getRemark(),
							zipDataInfo.size());
				}
				if (testingDataInfo == null){
					testingDataInfo = Lists.newArrayList();
				}
				testingDataInfo.add(testingData);
			}
		}

		if (listInfo.size() > 0) {
			alertListInfoImpl.insertAlertListInfo(listInfo);
		}
		if (info.size() > 0) {
			alertListInfoImpl.insertAlertInfo(info);
		}
		if (subInfo.size() > 0) {
			alertListInfoImpl.insertSubChannel(subInfo);
		}
		if (origInfo.size() > 0) {
			alertListInfoImpl.insertOriginal(origInfo);
		}
		if (zipDataInfo != null && zipDataInfo.size() > 0) {
			alertListInfoImpl.insertZipData(zipDataInfo);
		}

		if (testingDataInfo != null && testingDataInfo.size() > 0) {
			for (TestingResultData testData : testingDataInfo) {
				try {
					invokeExtractData(testData);
				} catch (Exception e) {
					logger.error("invokeExtractData error {}", e);
				}
			}
		}
	}

	/**
	 * getAlertInfo
	 *
	 * @param testingDatas
	 * @param alertListInfohandle
	 * @param info
	 * @return
	 */
	public Map<String, String> getAlertInfo(List<TestingResultData> testingDatas, String alertListInfohandle,
			List<AlertInfoModel> info) {
		String category;
		String processDataAlert = "false";
		String curr = "false";// 判断电流是否异常
		String volt = "false";// 判断电压是否异常
		String time = "false";// 判断时间是否异常
		String capa = "false";// 判断容量是否异常
		String temp = "false";// 判断温度是否异常
		Map<String, String> isCateAlertmap = Maps.newHashMap();

		for (TestingResultData testingData : testingDatas) {
			category = testingData.getCategory();// 场景
			if (testingData.getAlertLevel() != 0 && category != null && testingData.getHandle() != null) {
				AlertInfoModel alertInfoModel = new AlertInfoModel();
				switch (category) {
				case SystemConfig.CURRENTSCENENAME:
					curr = "true";
					processDataAlert = "true";
					break;
				case SystemConfig.VLOTAGESCENENAME:
					volt = "true";
					processDataAlert = "true";
					break;
				case SystemConfig.TIMESCENENAME:
					time = "true";
					processDataAlert = "true";
					break;
				case SystemConfig.CAPACITYSCENENAME:
					capa = "true";
					processDataAlert = "true";
					break;
				case SystemConfig.TEMPERATURESENENAME:
					temp = "true";
					processDataAlert = "true";
					break;
				default:
					temp = "false";
					processDataAlert = "false";
					break;
				}
				alertInfoModel.setAlertInfoHandle(testingData.getHandle());
				alertInfoModel.setSite(testingData.getSite());
				alertInfoModel.setRemark(testingData.getTestingMessage().getRemark());
				alertInfoModel.setSfc(testingData.getTestingMessage().getSfc());
				alertInfoModel.setCategory(category);
				alertInfoModel.setSequenceNumber(testingData.getAltetSequenceNumber());
				alertInfoModel.setAlertListInfohandle(alertListInfohandle); //// 待改
				alertInfoModel.setStatus(testingData.getStatus());
				alertInfoModel.setProcessDataBO(testingData.getProcessDataBO());
				alertInfoModel.setTimestamp(testingData.getTimestamp());
				alertInfoModel.setErpResourceBO(testingData.getErpResourceBO());
				alertInfoModel.setChannelId(testingData.getTestingMessage().getChannelId());
				alertInfoModel.setAlertLevel(testingData.getAlertLevel());
				alertInfoModel.setDescription(testingData.getDescription());
				alertInfoModel.setUpLimit(testingData.getUpLimit());
				alertInfoModel.setLowLimit(testingData.getLowLimit());
				alertInfoModel.setOriginalProcessDataBO(testingData.getOriginalProcessDataBO());
				// alertInfoModels.add(alertInfoModel);
				info.add(alertInfoModel);
			}
		}
		isCateAlertmap.put("curr", curr);
		isCateAlertmap.put("volt", volt);
		isCateAlertmap.put("capa", capa);
		isCateAlertmap.put("time", time);
		isCateAlertmap.put("temp", temp);
		isCateAlertmap.put("processDataAlert", processDataAlert);
		return isCateAlertmap;

	}

	/**
	 * @param testingResultData
	 * @param isCateAlertmap
	 * @param origInfo
	 * @param subInfo
	 */
	@SuppressWarnings("unchecked")
	public void getOrignalData(TestingResultData testingResultData, Map<String, String> isCateAlertmap,
			List<SubChannelModel> subInfo, List<OriginalDataModel> origInfo) {
		String subHandle1 = null;
		String subHandle2 = null;
		String subHandle3 = null;
		String subHandle4 = null;
		String subHandle5 = null;
		String subHandle6 = null;
		TestingMessage testingMsg = testingResultData.getTestingMessage();
		try {
			if (testingMsg != null && testingMsg.getSubChannel().size() > 0) {
				String procehandle = testingResultData.getOriginalProcessDataBO();
				for (TestingSubChannel testingSubChannel : testingMsg.getSubChannel()) {
					SubChannelModel subChannelModel = new SubChannelModel();
					int i = 0;
					switch (testingSubChannel.getSubChannelName()) {
					case "pvSubChannelData1":
						i = 1;
						subHandle1 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle1);
						break;
					case "pvSubChannelData2":
						i = 2;
						subHandle2 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle2);
						break;
					case "pvSubChannelData3":
						i = 3;
						subHandle3 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle3);
						break;
					case "pvSubChannelData4":
						i = 4;
						subHandle4 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle4);
						break;
					case "pvSubChannelData5":
						i = 5;
						subHandle5 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle5);
						break;
					case "pvSubChannelData6":
						i = 6;
						subHandle6 = StringUtils.join("TxOriginalSubChannelDataBO:", procehandle, ",", i);
						subChannelModel.setSubHandle(subHandle6);
						break;
					}
					subChannelModel.setRemark(testingMsg.getRemark());
					subChannelModel.setProcehandleBo(testingResultData.getOriginalProcessDataBO());
					subChannelModel.setSubChannelId(i);
					subChannelModel.setSite(testingResultData.getSite());
					subChannelModel.setRemark(testingMsg.getRemark());
					subChannelModel.setSfc(testingMsg.getSfc());
					subChannelModel.setResourceId(testingMsg.getResourceId());
					subChannelModel.setChannelId(testingMsg.getChannelId());
					subChannelModel.setSequenceId(testingSubChannel.getSequenceId());
					subChannelModel.setCycleId(testingSubChannel.getCycle());
					subChannelModel.setStepId(testingSubChannel.getStepId());
					subChannelModel.setTestTimeDuration(testingSubChannel.getTestTimeDuration());
					subChannelModel.setVoltage(testingSubChannel.getVoltage());
					subChannelModel.setCurrent(testingSubChannel.getCurrent());
					subChannelModel.setIr(testingSubChannel.getIr());
					subChannelModel.setTemperature(testingSubChannel.getTemperature());
					subChannelModel.setChargeCapacity(testingSubChannel.getChargeCapacity());
					subChannelModel.setDischargeCapacity(testingSubChannel.getDischargeCapacity());
					subChannelModel.setChargeEnergy(testingSubChannel.getChargeEnergy());
					subChannelModel.setDischargeEnergy(testingSubChannel.getDischargeEnergy());
					subChannelModel.setTimestamp(testingSubChannel.getTimestamp().toString());
					subChannelModel.setDataflag(testingSubChannel.getDataFlag());
					subChannelModel.setWorkType(testingSubChannel.getWorkType());
					subInfo.add(subChannelModel);
				}
			}
			OriginalDataModel originalDataModel = new OriginalDataModel();
			originalDataModel.setProcehandle(testingResultData.getOriginalProcessDataBO());
			originalDataModel.setSite(testingResultData.getSite());
			originalDataModel.setRemark(testingMsg.getRemark());
			originalDataModel.setSfc(testingMsg.getSfc());
			originalDataModel.setResourceId(testingMsg.getResourceId());
			originalDataModel.setChannelId(testingMsg.getChannelId());
			originalDataModel.setSequenceId(testingMsg.getSequenceId());
			originalDataModel.setCycle(testingMsg.getCycle());
			originalDataModel.setStepId(testingMsg.getStepId());
			originalDataModel.setStepName(testingMsg.getStepName());
			originalDataModel.setTestTimeDuration(testingMsg.getTestTimeDuration());
			originalDataModel.setTimestamp(testingMsg.getTimestamp().toString());
			originalDataModel.setSvIcRange(testingMsg.getSvIcRange());
			originalDataModel.setSvIvRange(testingMsg.getSvIvRange());
			originalDataModel.setPvVoltage(testingMsg.getPvVoltage());
			originalDataModel.setPvCurrent(testingMsg.getPvCurrent());
			originalDataModel.setPvIr(testingMsg.getPvIr());
			originalDataModel.setPvTemperature(testingMsg.getPvTemperature());
			originalDataModel.setPvChargeCapacity(testingMsg.getPvChargeCapacity());
			originalDataModel.setPvDischargeCapacity(testingMsg.getPvDischargeCapacity());
			originalDataModel.setPvChargeEnergy(testingMsg.getPvChargeEnergy());
			originalDataModel.setPvDischargeEnergy(testingMsg.getPvDischargeEnergy());
			originalDataModel.setSubChannelIdBO1(subHandle1);
			originalDataModel.setSubChannelIdBO2(subHandle2);
			originalDataModel.setSubChannelIdBO3(subHandle3);
			originalDataModel.setSubChannelIdBO4(subHandle4);
			originalDataModel.setSubChannelIdBO5(subHandle5);
			originalDataModel.setSubChannelIdBO6(subHandle6);
			originalDataModel.setDataflag(testingMsg.getPvDataFlag());
			originalDataModel.setWorkType(testingMsg.getPvWorkType());
			originalDataModel.setIsCapalert(isCateAlertmap.get("capa"));
			originalDataModel.setIsCurrAlert(isCateAlertmap.get("curr"));
			originalDataModel.setIsDataAlert(isCateAlertmap.get("processDataAlert"));
			originalDataModel.setIsTempAlert(isCateAlertmap.get("temp"));
			originalDataModel.setIsTimeAlert(isCateAlertmap.get("time"));
			originalDataModel.setIsVoltlert(isCateAlertmap.get("volt"));
			originalDataModel.setRootRemark(testingResultData.getRootRemark());
			originalDataModel.setBusinessId(testingMsg.getBusinessCycle());
			originalDataModel.setStepLogicNumber(testingMsg.getStepLogicNumber());
			origInfo.add(originalDataModel);
		} catch (Exception e) {
			logger.error("getOrignalData error,remark:{},sequenceId:{},error message:{}", testingResultData.getRemark(),
					testingResultData.getTestingMessage().getSequenceId(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void setZipData(TestingResultData testingResultData, List<ZipDataModel> zipDatainfos) {
		ZipDataModel zipDataModel = new ZipDataModel();
		zipDataModel.setRemark(testingResultData.getTestingMessage().getRemark());
		zipDataModel.setSite(testingResultData.getSite());
		zipDataModel.setZipHandle(StringUtils.join("TechZipStatusBO:", testingResultData.getSite(), ",",
				testingResultData.getTestingMessage().getRemark(), ",",
				testingResultData.getTestingMessage().getBusinessCycle(), ",",
				testingResultData.getTestingMessage().getStepId()));
		zipDataModel.setStepId(testingResultData.getTestingMessage().getStepId());
		zipDataModel.setBusinessId(testingResultData.getTestingMessage().getBusinessCycle());
		zipDatainfos.add(zipDataModel);
	}

	public void invokeExtractData(TestingResultData testingResultData) {
		hanaDataHandler.extractData(testingResultData.getTestingMessage().getRemark(),
				testingResultData.getTestingMessage().getBusinessCycle(),
				testingResultData.getTestingMessage().getStepId());
	}

}
