package com.edcs.tds.storm.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.dao.ResultDataDao;
import com.edcs.tds.storm.model.ExecuteContext;
import com.edcs.tds.storm.model.TestingResultData;
import com.google.common.collect.Maps;

/**
 * 结果数据同步服务
 *
 * @author LIQF
 */
public class SyncResultDataService {
	private static final Logger logger = LoggerFactory.getLogger(SyncResultDataService.class);

	private ConcurrentMap<String, List<TestingResultData>> dataQuene = Maps.newConcurrentMap();
	private ResultDataDao resultDataDao;

	private static final int NUMBER = 100;

	public void setResultDataDao(ResultDataDao resultDataDao) {
		this.resultDataDao = resultDataDao;
	}

	public SyncResultDataService() {
		start();
	}

	public void save(ExecuteContext executeContext) {
		String key = executeContext.getMessage().getMessageId();
		dataQuene.put(key, executeContext.getResultDatas());
	}

	public void flush() throws Exception {
		ConcurrentMap<String, List<TestingResultData>> list = Maps.newConcurrentMap();
		int i = 0;
		for (String messageId : dataQuene.keySet()) {
			i++;
			List<TestingResultData> testingDatas = dataQuene.remove(messageId);
			list.put(messageId, testingDatas);
			if (i > NUMBER) {
				break;
			}
		}
		resultDataDao.setAllData(list);

	}

	public void start() {
		ResultDataWriteThread thread = new ResultDataWriteThread();
		thread.setName(StringUtils.<Object> join("DBWriteThread-", "-", UUID.randomUUID()));
		thread.start();

	}

	private class ResultDataWriteThread extends Thread {
		@Override
		public void run() {
			while (true) {
				if (dataQuene.size() > NUMBER) {
					try {
						flush();
					} catch (Exception e) {
						logger.error("flush data to hana error：", e);
						// 将插入hana出现错误的测试结果数据写入redis
					}
				} else {
					try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
				}
			}

		}
		@Override
		public void interrupt() {
			super.interrupt();
			try {
				flush();
			} catch (Exception e) {
				logger.error("ResultDataWriteThread interrupt is error ,error msg:", e);
			}
		}
		@SuppressWarnings("deprecation")
		@Override
		public void destroy() {
			super.destroy();
			try {
				flush();
			} catch (Exception e) {
				logger.error("ResultDataWriteThread destroy is error ,error msg:", e);
			}
		}
	}
}
