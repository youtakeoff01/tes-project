package com.edcs.tds.storm.redis;

import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.edcs.tds.storm.model.SystemConfig;
import com.google.common.base.Joiner;

import redis.clients.util.SafeEncoder;

public class RedisCacheKey {

	private static Joiner joiner = Joiner.on(":").skipNulls();

	public static final String ROOT = "TES";
	public static final String CONFIG = "CONFIG";
	public static final String LOGIC = "LOGIC";
	public static final String FILTER = "FILTER";
	public static final String SEQUENCEID = "sequenceId";
	public static final String MD = "MD";
	public static final String CYCLE = "businessCycle";
	public static final String STEPLOGICNUM = "stepLogicNumber";
	public static final String RESULTDATE = "tempResultData";
	public static final String ALTERLEVEL = "alterLevel";
	public static final String WARNINGLEVEL = "warningLevel";

	public static final String ALERTLISTINFOSTATE = "alertListInfoState";
	public static final String LISTINFOHANDLE = "listInfoHandle";
	
	public static final String MIDCOMEKEY = "midcomekey";
	public static final String CYCLEVALUE = "cyclevalue";


	public static byte[] getRuleConfig() {
		return SafeEncoder.encode(joiner.join(ROOT, CONFIG, "RULE"));
	}

	public static String getAlertListInfoState(String remark) {
		return joiner.join(ROOT, ALERTLISTINFOSTATE, remark);
	}
	public static String getListInfoHandle(String remark){
		return joiner.join(ROOT,LISTINFOHANDLE,remark);
	}

	public static String getRuleConfigVersion() {
		return joiner.join(ROOT, CONFIG, "RULE", "VERSION");
	}

	public static String getMDProcessKey() {
		return joiner.join(ROOT, CONFIG, "PRO");
	}

	public static String getMDStepKey() {
		return joiner.join(ROOT, CONFIG, "STEP");
	}

	public static String getMDSubKey() {
		return joiner.join(ROOT, CONFIG, "SUB");
	}

	public static String getMDChangeKey() {
		return joiner.join(ROOT, CONFIG, "CHANGE");
	}

	public static String getUpStepInfoKey(String key,int logicNumber){return  joiner.join(ROOT,LOGIC,key,logicNumber);};

	public static String getUpStepInfoValue(String stepName,String svCurrent){return  joiner.join(stepName,svCurrent);}

	public static String getFilterKey(String key) {
		return joiner.join(ROOT, FILTER, key);
	}

	public static String getDataSyncKey() {
		return joiner.join(ROOT, "RESULT");
	}
	
	public static String getCycleKey(String key,int stepId){
		return joiner.join(ROOT,CYCLEVALUE,key,stepId);
	}
	public static String getIsMidComeKey(String key){
		return joiner.join(ROOT,MIDCOMEKEY,key);
	}

	public static String getSequenceQueue(String key) {
		return joiner.join(ROOT, SEQUENCEID, key);
	}

	public static String getMasterData(String key) {
		return MD + "_" + key;
	}

	public static String getCycleQueue(String key) {
		return joiner.join(ROOT, CYCLE, key);
	}

	public static String getStepLogicQueue(String key) {
		return joiner.join(ROOT, STEPLOGICNUM, key);
	}

	public static String getTempResultData(String key, String sequenceId) {
		return joiner.join(ROOT, RESULTDATE, key, sequenceId);
	}

	public static String getAlterNumber(String remark, String resourceId, int channelId, String sceneName) {
		return joiner.join(ROOT, ALTERLEVEL, remark, resourceId, channelId, sceneName);
	}

	public static String getReceiverKey(int alterLe) {
		return WARNINGLEVEL + "_" + alterLe;
	}

	public static String getTimeConstantPKey(String remark) {
		//TES:T3-20171028-2161-793201_Cycle_3_C0-F100_45769:恒流恒压充电
		return joiner.join(ROOT, remark, SystemConfig.CONSTANT_CURRENT_VOLTAGE_CHARGE);
	}

}