package com.edcs.tds.storm.topology.calc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.cache.ScriptCacheService;
import com.edcs.tds.storm.core.ScriptExecutor;
import com.edcs.tds.storm.model.EmailEntity;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingResultData;
import com.edcs.tds.storm.model.UserIntegrationRedis;
import com.edcs.tds.storm.redis.RedisCacheKey;
import com.edcs.tds.storm.redis.RedisClient;
import com.edcs.tds.storm.util.CommonUtil;
import com.edcs.tds.storm.util.JsonUtils;
import com.edcs.tds.storm.util.SendEmailTask;

import groovy.lang.Binding;
import groovy.lang.Script;

public class RuleCalc {

	private static final Logger logger = LoggerFactory.getLogger(RuleCalc.class);

	public List<TestingResultData> testingRuleCalc(ScriptExecutor scriptExecutor, TestingMessage testingMessage,
			Binding shellContext, MasterData masterData, ExecutorService cachedThreadPool,
			RedisClient redisClient) {
		List<TestingResultData> listResult = new ArrayList<TestingResultData>();// 用来存放结果数据
		// 开始规则计算匹配
		try {
			// 获取测试数据对应的主数据工步的所有的场景
			Map<String, Script> scripts = ScriptCacheService.getScript(testingMessage, masterData);
			if (scripts == null) {
				logger.warn("can not find script from local cache, remark:{}.", testingMessage.getRemark());
				return listResult;
			}
			for (Entry<String, Script> entry : scripts.entrySet()) {
				String scriptKey = entry.getKey();
				String sceneName = scriptKey;// 场景名称
				String alterLevel = null;// 报警返回的信息
				try {
					Script script = entry.getValue();
					script.setBinding(shellContext);
					// 返回值为 ： 报警上限_报警下限_比较值_报警级别
					alterLevel = scriptExecutor.execute(script);
					logger.debug("execute script, result:{}.", alterLevel);
				} catch (Exception e) {
					logger.error("Rule execute error, remark:" + testingMessage.getRemark() + ", step id:"
							+ testingMessage.getStepId() + ", scene name:" + sceneName, e);
				}
				if (StringUtils.isNotEmpty(alterLevel)) {// 表明有报警情况
					listResult.add(createResultData(masterData, testingMessage, alterLevel, sceneName,
							cachedThreadPool, redisClient));
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return listResult;
	}

	/**
	 * 创建测试结果数据
	 *
	 * @param mDprocessInfo
	 * @param testingMessage
	 * @param alterLevel
	 * @paramproxyJedisPool
	 * @param sceneName
	 * @param cachedThreadPool
	 * @return
	 * @throws Exception
	 */
	private TestingResultData createResultData(MasterData mDprocessInfo, TestingMessage testingMessage,
			String alterLevel, String sceneName, ExecutorService cachedThreadPool, RedisClient redisClient)
			throws Exception {
		int sequenceNumber = 1;// 同一个流程同一个场景的报警次数
		String alterNumberKey = RedisCacheKey.getAlterNumber(testingMessage.getRemark(), testingMessage.getResourceId(),
				testingMessage.getChannelId(), sceneName);
		TestingResultData testingResultData = new TestingResultData();
		testingResultData.setRemark(testingMessage.getRemark());
		StringBuffer content = new StringBuffer();
		Set<String> sets = null;
		int alterLe = 0;
		BigDecimal upLimit = null;
		BigDecimal lowLimit = null;
		// 获取同一个流程上面测试数据的scenecName场景的报警次数
		String alterNumber = redisClient.get(alterNumberKey, SystemConfig.OVERTIME);
		if (StringUtils.isNotEmpty(alterNumber)) {
			int newSequenceNumber = Integer.valueOf(alterNumber);
			sequenceNumber = newSequenceNumber + 1;
		}
		redisClient.set(alterNumberKey, String.valueOf(sequenceNumber), null);
		// 调用发送邮件接口发送预警信息 -- start
		// 通过redis获取收件人信息
		String receiverKey = RedisCacheKey.getReceiverKey(alterLe);
		sets = redisClient.smembers(receiverKey);
		// 将结果集写入Redis缓存
		String[] alters = alterLevel.split("_");
		/*upLimit = new BigDecimal(alters[0]); .valueOf(Long.valueOf(alters[0]));//报警上限
		lowLimit = new BigDecimal(alters[1]); // .valueOf(Long.valueOf(alters[1]));//报警下限*/
        upLimit = new BigDecimal(alters[0]).compareTo(new BigDecimal(alters[1])) == 1?new BigDecimal(alters[0]) : new BigDecimal(alters[1]); // .valueOf(Long.valueOf(alters[0]));//报警上限
        lowLimit = new BigDecimal(alters[0]).compareTo(new BigDecimal(alters[1])) != 1?new BigDecimal(alters[0]) : new BigDecimal(alters[1]); // .valueOf(Long.valueOf(alters[1]));//报警下限

        alterLe = Integer.parseInt(alters[3]);// 报警级别
		// 预警信息。
		content.append("设备号为：");
		content.append(testingMessage.getResourceId());
		content.append(";</br>");
		content.append("通道号为：");
		content.append(testingMessage.getChannelId());
		content.append(";</br>");
		content.append("流程号为：");
		content.append(testingMessage.getRemark());
		content.append(";</br>");
		content.append("工步名称为：");
		content.append(testingMessage.getStepName());
		content.append(";</br>");
		content.append("场景名称为：");
		content.append(sceneName);
		content.append(";</br>");
		content.append("产生了");
		content.append(alterLe);
		content.append("级预警！");

		if (sets != null && sets.size() > 0) {
			sendEmail(sets, content.toString(), cachedThreadPool);// 异步发送。
		}
		// 调用发送邮件接口发送预警信息 -- end
		String site = mDprocessInfo.getSite();
		testingResultData.setSite(site);
		testingResultData.setCategory(sceneName);
		testingResultData.setAltetSequenceNumber(sequenceNumber);
		testingResultData.setStatus("new");
		// MdProcessInfoBO:<SITE>,<PROCESS_ID>,<REMARK>
		testingResultData.setProcessDataBO(mDprocessInfo.getHandle());
		testingResultData.setTimestamp(new Timestamp(new Date().getTime()));
		// ErpResourceBO:<SITE>,<RESOURCE_ID>
		testingResultData
				.setErpResourceBO(CommonUtil.commaLink().join("ErpResourceBO:" + site, testingMessage.getResourceId()));
		String handle = CommonUtil.commaLink().join("TxAlertInfoBO:" + site, mDprocessInfo.getRemark(),
				testingMessage.getSfc(), sceneName, sequenceNumber, testingResultData.getErpResourceBO(),
				testingMessage.getChannelId(),System.currentTimeMillis());

		testingResultData.setHandle(handle);
		testingResultData.setAlertLevel(alterLe);
		testingResultData.setDescription(content.toString());
		testingResultData.setUpLimit(upLimit);
		testingResultData.setLowLimit(lowLimit);
		// TxOriginalProcessDataBO:<SITE>,<REMARK>,<SFC>
		// ,<RESOURCE_ID>,<CHANNEL_ID>,<SEQUENCE_ID>
		testingResultData.setOriginalProcessDataBO(CommonUtil.commaLink().join(
				"TxOriginalProcessDataBO:" + mDprocessInfo.getSite(), mDprocessInfo.getRemark(), mDprocessInfo.getSfc(),
				testingMessage.getResourceId(), testingMessage.getChannelId(), testingMessage.getSequenceId()));
		testingResultData.setCreatedDateTime(mDprocessInfo.getCreateDateTime());
		testingResultData.setCreatedUser(mDprocessInfo.getCreateUser());
		testingResultData.setModifiedDateTime(mDprocessInfo.getModifiedDateTime());
		testingResultData.setModifiedUser(mDprocessInfo.getCreateUser());
		testingResultData.setRootRemark(mDprocessInfo.getRootRemark());
		testingResultData.setTestingMessage(testingMessage);
		testingResultData.setIsContainMainData("1");// 表示匹配上主数据
		return testingResultData;
	}

	/**
	 * 发送邮件到指定的人
	 *
	 * @param sets
	 * @param cachedThreadPool
	 */
	private void sendEmail(Set<String> sets, String content, ExecutorService cachedThreadPool) {
		try {
			// 发送邮件
			List<String> receiveAccounts = new ArrayList<String>();// 存放收件人帐号
			EmailEntity emailEntity = new EmailEntity();
			for (String string2 : sets) {
				UserIntegrationRedis userMsg = JsonUtils.toObject(string2, UserIntegrationRedis.class);
				receiveAccounts.add(userMsg.getEmail());
			}
			emailEntity.setReceiveAccounts(receiveAccounts);
			emailEntity.setBooeanSsl(SystemConfig.IS_BOOEAN_SSL);
			emailEntity.setContent(content);
			emailEntity.setSendServer(SystemConfig.MY_EMAIL_SMTPHOST);
			emailEntity.setSendAccount(SystemConfig.MY_EMAIL_ACCOUNT);
			emailEntity.setEmailPassword(SystemConfig.MY_EMAIL_PASSWORD);
			cachedThreadPool.execute(new SendEmailTask(emailEntity));// 异步发送邮件
		} catch (Exception e) {
			logger.error("send email error", e);
		}
	}
}