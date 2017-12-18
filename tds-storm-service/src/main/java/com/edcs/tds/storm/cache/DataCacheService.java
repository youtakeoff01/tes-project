package com.edcs.tds.storm.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.model.MasterData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;

public class DataCacheService {

	private static final Logger logger = LoggerFactory.getLogger(DataCacheService.class);

	private static LoadingCache<String, MasterData> masterDataCache = null;

	private static ConcurrentMap<String, String> valueVersion = Maps.newConcurrentMap();

	public static LoadingCache<String, MasterData> createCache() {
		if (masterDataCache == null) {
			masterDataCache = CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterAccess(30, TimeUnit.DAYS)
					.initialCapacity(150).maximumSize(14000).removalListener(new RemovalListener<Object, Object>() {
						public void onRemoval(RemovalNotification<Object, Object> notification) {
							logger.info(notification.getKey() + " was removed, cause is " + notification.getCause());
							valueVersion.remove(notification.getKey());//清除对应的版本信息
						}
					}).build(new CacheLoader<String, MasterData>() {

						@Override
						public MasterData load(String key) throws Exception {
							return null;
						}
					});
			logger.info("create testing master data cache.");
		}
		return masterDataCache;
	}

	public static LoadingCache<String, MasterData> getCachePool() {
		return createCache();
	}

	public static void removeMasterData(String key, String version) {
		try {
			getCachePool().invalidate(key);
		} catch (Exception e) {
			logger.error("remove master data cache error", e);
		}
//		valueVersion.remove(key);
//		logger.info("remove testing master data, key:{}, cache state:{}.", version, getCachePool().stats());
	}
	
	public static void removeMasterData(String key) {
		try {
			getCachePool().invalidate(key);
		} catch (Exception e) {
			logger.error("remove master data cache error", e);
		}
	}

	public static MasterData getMasterData(String key, String version) throws ExecutionException {
		if (valueVersion.get(key) != null && valueVersion.get(key).equals(version)) {
			logger.info("get master data from local cache, remark:{}.", key);
			return getCachePool().get(key);
		} else {
			removeMasterData(key, version);
			//清除这条主数据对应的脚本信息
			ScriptCacheService.cleanInvalidCache(key);
		}
		return null;
	}

	public static void addMasterData(String key, MasterData value, String version) throws ExecutionException {
		getCachePool().put(key, value);
		valueVersion.put(key, version);
		logger.info("put testing master data to cache, key:{}, cache state:{}.", key, getCachePool().stats());
	}

	public static CacheStats cacheStatus() {
		if (masterDataCache != null) {
			return masterDataCache.stats();
		}
		return null;
	}

	public static void invalidateAll() {
		if (masterDataCache != null) {
			masterDataCache.invalidateAll();
		}
	}
}
