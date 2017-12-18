package com.edcs.tds.storm.cache;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.redis.RedisClient;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
/**
 * 用来过滤工况数据
 * @author LIQF
 *
 */
public class FilterMappingService {

	private static final Logger logger = LoggerFactory.getLogger(FilterMappingService.class);

	private static LoadingCache<String, String> mappingCache = null;

	public static final String REDIS_KEY = "TES:FILTER:MAPPING";

	public static LoadingCache<String, String> createCache() {
		if (mappingCache == null) {
			mappingCache = CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterWrite(2, TimeUnit.HOURS)
					.initialCapacity(50).maximumSize(200).removalListener(new RemovalListener<Object, Object>() {
						public void onRemoval(RemovalNotification<Object, Object> notification) {
							logger.info(notification.getKey() + " was removed, cause is " + notification.getCause());
						}
					}).build(new CacheLoader<String, String>() {

						@Override
						public String load(String key) throws Exception {
							return null;
						}
					});
			logger.info("create testing master data cache.");
		}
		return mappingCache;
	}
	
	public static CacheStats cacheStatus() {
		if (mappingCache != null) {
			return mappingCache.stats();
		}
		return null;
	}

	public static void invalidateAll() {
		if (mappingCache != null) {
			mappingCache.invalidateAll();
		}
	}

	public static void initFilterMapping(RedisClient redisClient) throws ExecutionException {
		if (mappingCache.size() <= 0) {
			Set<String> mapping = Sets.newHashSet();
			mapping = redisClient.smembers(REDIS_KEY);
            if(mapping!=null){
            	for (String s : mapping) {
            		mappingCache.put(s, s);
            	}
            	logger.info("filter mapping:{}", mapping.toString());
            }
		}
	}

	public static boolean filterMessage(String resourceId) throws ExecutionException {
		boolean flag = false;
		try {
			flag = mappingCache.get(resourceId) != null;
		} catch (Exception e) {
			// NOTHING TO DO
		}
		return flag;
	}

	public static void main(String[] args) throws ExecutionException {

	}
}
