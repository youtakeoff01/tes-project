package com.edcs.tds.storm.redis;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

public class RedisClient {

	private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

	private ProxyJedisPool proxyJedisPoolFir = null;
	private ProxyJedisPool proxyJedisPoolSec = null;
	private int retryCount = 0;

	public void setProxyJedisPool(ProxyJedisPool proxyJedisPoolFir, ProxyJedisPool proxyJedisPoolSec) {
		this.proxyJedisPoolFir = proxyJedisPoolFir;
		this.proxyJedisPoolSec = proxyJedisPoolSec;
	}
	
	public void setProxyJedisPoolFir(ProxyJedisPool proxyJedisPoolFir) {
		this.proxyJedisPoolFir = proxyJedisPoolFir;
	}
	
	public void setProxyJedisPoolSec(ProxyJedisPool proxyJedisPoolSec) {
		this.proxyJedisPoolSec = proxyJedisPoolSec;
	}

	public synchronized Jedis getConn() {
		retryCount++;
		Jedis conn = null;
		if (retryCount > 3) {
			logger.warn("get redis connection error,over try num={}",retryCount);
			retryCount = 0;
			return conn;
		}
		try {
			conn = proxyJedisPoolFir.getResource();
		} catch (Exception e) {
			logger.error("get proxyJedisPoolFir connection error, connection pool state: active:{}, idel:{}.", proxyJedisPoolFir,
					proxyJedisPoolFir.getNumIdle());
			try {
				conn = proxyJedisPoolSec.getResource();
			} catch (Exception e2) {
				logger.error("get proxyJedisPoolSec connection error, connection pool state: active:{}, idel:{}.",
						proxyJedisPoolSec, proxyJedisPoolSec.getNumIdle());
				getConn();
			}
		}
		logger.info("get redis connection success,over try num={}",retryCount);
		retryCount = 0;
		return conn;
	}

	public String get(String key, Integer overTime) {
		Jedis jedis = getConn();
		String value = null;
		try {
			if (jedis != null) {
				value = jedis.get(key);
				if (overTime != null) {
					jedis.expire(key, overTime);
				}
			}
		} catch (Exception e) {
			logger.error("get from redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return value;
	}

	public byte[] get(byte[] key, Integer overTime) {
		byte[] bytes = null;
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				bytes = jedis.get(key);
				if (overTime != null) {
					jedis.expire(key, overTime);
				}
			}
		} catch (Exception e) {
			logger.error("get bytes from redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return bytes;
	}

	public String set(String key, String value, Integer overTime) {
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				jedis.set(key, value);
				if (overTime != null) {
					jedis.expire(key, overTime);
				}
			}
		} catch (Exception e) {
			logger.error("set to redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return value;
	}

	public void set(byte[] encode, byte[] asByteArrayForDefault, byte[] encode2, byte[] encode3, int overTime) {
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				jedis.set(encode, asByteArrayForDefault, encode2, encode3, overTime);
			}
		} catch (Exception e) {
			logger.error("set to redis is error ,key={},errorMsg={}", SafeEncoder.encode(encode), e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
	}

	public Set<String> smembers(String key) {
		Jedis jedis = getConn();
		Set<String> values = null;
		try {
			if (jedis != null) {
				values = jedis.smembers(key);
			}
		} catch (Exception e) {
			logger.error("smembers from redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return values;
	}

	public long setnx(String key, String value, Integer overTime) {
		Jedis jedis = getConn();
		long valueLong = 0;
		try {
			if (jedis != null) {
				valueLong = jedis.setnx(key, value);
				if (overTime != null) {
					jedis.expire(key, overTime);
				}
			}
		} catch (Exception e) {
			logger.error("setnx to redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return valueLong;
	}

	public void closeQuietly(Jedis conn) {
		if (conn != null) {
			conn.close();
			conn = null;
		}
	}

	public void del(String key) {
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				jedis.del(key);
			}
		} catch (Exception e) {
			logger.error("del from redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
	}

	public void del(String... keys) {
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				jedis.del(keys);
			}
		} catch (Exception e) {
			logger.error("del keys from redis is error ,key={},errorMsg={}", keys.toString(), e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
	}


	public boolean exists(String key, Integer overTime) {
		boolean boo = true;
		Jedis jedis = getConn();
		try {
			if (jedis != null) {
				boo = jedis.exists(key);
				if (boo && overTime != null) {
					jedis.expire(key, overTime);
				}
			}
		} catch (Exception e) {
			logger.error("exists to redis is error ,key={},errorMsg={}", key, e.getMessage());
		} finally {
			closeQuietly(jedis);
		}
		return boo;
	}
}
