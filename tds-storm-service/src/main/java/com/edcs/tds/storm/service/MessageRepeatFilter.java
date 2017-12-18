package com.edcs.tds.storm.service;

import com.edcs.tds.storm.redis.RedisCacheKey;
import com.edcs.tds.storm.redis.RedisClient;

public class MessageRepeatFilter {

	private int expireTime = 60 * 60 * 3;
	private RedisClient redisClient;

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	public boolean filter(String key) {
		boolean flag = false;
		String jedisKey = RedisCacheKey.getFilterKey(key);
		long count = redisClient.setnx(jedisKey, String.valueOf(System.currentTimeMillis()), expireTime);
		if (count > 0) {
			flag = true;
		}
		return flag;
	}
}
