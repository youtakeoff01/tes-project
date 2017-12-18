package com.edcs.tds.storm.cache;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * 存放工步的前两条数据或者暂停、保护之后的前两条数据的sequenceId值
 * @author LIQF
 *
 */
public class FilterCacheService {
	private static final Logger logger = LoggerFactory.getLogger(FilterCacheService.class);
	
	private static LoadingCache<String, Integer> firstSeqCache = null;
	
	public static LoadingCache<String, Integer> createCache() {
		if (firstSeqCache == null) {
			firstSeqCache = CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterAccess(60, TimeUnit.DAYS)
					.initialCapacity(50).maximumSize(200).removalListener(new RemovalListener<Object, Object>() {
						public void onRemoval(RemovalNotification<Object, Object> notification) {
							logger.info(notification.getKey() + " was removed, cause is " + notification.getCause());
						}
					}).build(new CacheLoader<String, Integer>() {

						@Override
						public Integer load(String key) throws Exception {
							System.out.println("load.........");
							return 999999999;
						}
					});
			logger.info("create testing master data cache.");
		}
		return firstSeqCache;
	}
	
	public static void deleteSeqCache(String key){
		firstSeqCache.invalidate(key);
	}
	
	public static void setFirstSeqCache(String key, Integer sequenceId) {
		firstSeqCache.put(key, sequenceId);
	}
	
	public static BigDecimal getRelativeSeq(String key,Integer sequenceId){
		BigDecimal i = null;
		try {
			Integer seq = null;
			if((seq = firstSeqCache.get(key))!=999999999){
				i = new BigDecimal(sequenceId-seq);
			}
		} catch (Exception e) {
			logger.error("getRelativeSeq method error,errorMsg:{}",e);
		}
		return i;
	}
}
