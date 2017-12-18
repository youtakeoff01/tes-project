package com.edcs.tds.storm.redis;


import java.util.List;

import redis.clients.jedis.JedisPoolConfig;

public class JedisFactory {

//	private static final Logger logger = LoggerFactory.getLogger(JedisFactory.class);

	private Boolean blockWhenExhausted = null;
	private Integer maxIdle = 10;
	private Integer maxTotal = 300;
	private Long maxWaitMillis = 1000L;
	private Long minEvictableIdleTimeMillis = null;
	private Integer minIdle = 5;
	private Integer numTestsPerEvictionRun = null;
	private Boolean testOnBorrow = true;
	private Boolean testOnCreate = false;
	private Boolean testOnReturn = false;
	private Boolean testWhileIdle = null;
	private Long timeBetweenEvictionRunsMillis = null;
	private int timeout = 2000;

	private List<String> addressList;

	public void setBlockWhenExhausted(Boolean blockWhenExhausted) {
		this.blockWhenExhausted = blockWhenExhausted;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}
	

	public void setMaxWaitMillis(Long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public void setTestOnCreate(Boolean testOnCreate) {
		this.testOnCreate = testOnCreate;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setAddressList(List<String> addressList) {
		this.addressList = addressList;
	}

	private JedisPoolConfig getJedisPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if (blockWhenExhausted != null) {
			poolConfig.setBlockWhenExhausted(blockWhenExhausted);
		}
		if (maxIdle != null) {
			poolConfig.setMaxIdle(maxIdle);
		}
		if (maxTotal != null) {
			poolConfig.setMaxTotal(maxTotal);
		}
		if (maxWaitMillis != null) {
			poolConfig.setMaxWaitMillis(maxWaitMillis);
		}
		if (minEvictableIdleTimeMillis != null) {
			poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		}
		if (minIdle != null) {
			poolConfig.setMinIdle(minIdle);
		}
		if (numTestsPerEvictionRun != null) {
			poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		}
		if (testOnBorrow != null) {
			poolConfig.setTestOnBorrow(testOnBorrow);
		}
		if (testOnCreate != null) {
			poolConfig.setTestOnCreate(testOnCreate);
		}
		if (testOnReturn != null) {
			poolConfig.setTestOnReturn(testOnReturn);
		}
		if (testWhileIdle != null) {
			poolConfig.setTestWhileIdle(testWhileIdle);
		}
		if (timeBetweenEvictionRunsMillis != null) {
			poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		}
		return poolConfig;
	}

//	public List<JedisPool> createBasicJedisPool() {
//		Preconditions.checkArgument((addressList != null && !addressList.isEmpty()), "未定义addressList列表");
//		List<JedisPool> addrGroup = Lists.newArrayListWithCapacity(addressList.size());
//		JedisPoolConfig poolConfig = getJedisPoolConfig();
//		for (String addr : addressList) {
//			String[] ss = StringUtils.split(addr, ':');
//			JedisPool jedisPool = new JedisPool(poolConfig, ss[0], Integer.parseInt(ss[1]), timeout);
//			addrGroup.add(jedisPool);
//		}
//		return addrGroup;
//	}

	public ProxyJedisPool createProxyJedisPool() {
		JedisPoolConfig poolConfig = getJedisPoolConfig();
		ProxyJedisPool jedisPool = new ProxyJedisPool(poolConfig, addressList, timeout);
		return jedisPool;
	}
}