package com.edcs.tds.storm.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.storm.shade.com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.core.ScriptExecutor;
import com.edcs.tds.storm.model.MDStepInfo;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;

import groovy.lang.Script;

public class ScriptCacheService {

	private static final Logger logger = LoggerFactory.getLogger(ScriptCacheService.class);
	
	private static LoadingCache<String, Map<String, Script>> scriptMap = null;// 缓存脚本信息
	
	private static Joiner joiner = Joiner.on("_").skipNulls();
	
	
	
	public static LoadingCache<String, Map<String, Script>> createCache() {
		if (scriptMap == null) {
			scriptMap = CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterAccess(30, TimeUnit.DAYS)
					.initialCapacity(1500).maximumSize(1000000).removalListener(new RemovalListener<Object, Object>() {
						public void onRemoval(RemovalNotification<Object, Object> notification) {
							logger.info(notification.getKey() + " was removed, cause is " + notification.getCause());
						}
					}).build(new CacheLoader<String, Map<String, Script>>() {

						@Override
						public Map<String, Script> load(String key){
							Map<String,Script> map = new HashMap<String,Script>();
							map.put(SystemConfig.VLOTAGESCENENAME, null);
							map.put(SystemConfig.CURRENTSCENENAME, null);
							map.put(SystemConfig.TIMESCENENAME, null);
							map.put(SystemConfig.CAPACITYSCENENAME, null);
							return map;
						}
					});
		}
		return scriptMap;
	}

	public static LoadingCache<String, Map<String, Script>> getCachePool() {
		return createCache();
	}

	/**
	 * 获取脚本信息
	 * 
	 * @param key
	 * @return
	 * @throws ExecutionException 
	 */
	public static Map<String, Script> getScript(TestingMessage testingMsg, MasterData masterData) throws ExecutionException {
		String key = joiner.join(testingMsg.getRemark(),testingMsg.getStepId());
		Map<String, Script> scripts = null;
		// 首先到缓存中获取
		scripts = scriptMap.get(key);
		if (scripts.get(SystemConfig.VLOTAGESCENENAME)==null && scripts.get(SystemConfig.CURRENTSCENENAME)==null && scripts.get(SystemConfig.TIMESCENENAME)==null && scripts.get(SystemConfig.CAPACITYSCENENAME)==null) {
			scripts = mDRuleConfig(masterData, testingMsg.getStepId());// 写到缓存中
		}
		return scripts;
	}


	/**
	 * 重构ruleconfig
	 */
	public static Map<String, Script> mDRuleConfig(MasterData masterData, int stepId) {
		Map<String, Script> map = Maps.newConcurrentMap();
		try {
			if (masterData != null && masterData.getStepData().size() > 0) {
				Map<Integer, MDStepInfo> mdStepInfoList = masterData.getStepData();
				MDStepInfo mdStepInfo = mdStepInfoList.get(stepId);
				String scriptVoltage = mdStepInfo.getScriptVoltage();
				try {
					Script vScript = ScriptExecutor.getDefaultShell().parse(scriptVoltage);
					map.put(SystemConfig.VLOTAGESCENENAME, vScript);
				} catch (Exception e) {
					logger.error("remark：" + masterData.getRemark() + "stepId：" + mdStepInfo.getStepId()
							+ "V error:", e);
				}
				String scriptCurrent = mdStepInfo.getScriptCurrent();
				try {
					Script iScript = ScriptExecutor.getDefaultShell().parse(scriptCurrent);
					map.put(SystemConfig.CURRENTSCENENAME, iScript);
				} catch (Exception e) {
					logger.error("remark：" + masterData.getRemark() + "stepId：" + mdStepInfo.getStepId()
							+ "I error:", e);
				}
				String scriptTime = mdStepInfo.getScriptTime();
				try {
					Script tScript = ScriptExecutor.getDefaultShell().parse(scriptTime);
					map.put(SystemConfig.TIMESCENENAME, tScript);
				} catch (Exception e) {
					logger.error("remark：" + masterData.getRemark() + "stepId：" + mdStepInfo.getStepId()
							+ "D error:", e);
				}
				String scriptCapacity = mdStepInfo.getScriptCapacity();
				try {
					Script cScript = ScriptExecutor.getDefaultShell().parse(scriptCapacity);
					map.put(SystemConfig.CAPACITYSCENENAME, cScript);
				} catch (Exception e) {
					logger.error("remark：" + masterData.getRemark() + "stepId：" + mdStepInfo.getStepId()
							+ "C error:", e);
				}
				String key = joiner.join(masterData.getRemark(),stepId);
				scriptMap.put(key, map);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return map;
	}

	/**
	 * 清理无效的脚本缓存
	 */
	public static void cleanInvalidCache(String remark) {
		Iterator<Map.Entry<String, Map<String, Script>>> it = scriptMap.asMap().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Map<String, Script>> entry = it.next();
			String key = entry.getKey();
			if (key.contains(remark)) {
				it.remove();
			}
		}
	}
}
