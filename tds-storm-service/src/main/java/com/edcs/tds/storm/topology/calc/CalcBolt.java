package com.edcs.tds.storm.topology.calc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.edcs.tds.storm.cache.DataCacheService;
import com.edcs.tds.storm.cache.FilterCacheService;
import com.edcs.tds.storm.cache.FilterMappingService;
import com.edcs.tds.storm.cache.ScriptCacheService;
import com.edcs.tds.storm.core.ScriptExecutor;
import com.edcs.tds.storm.model.ExecuteContext;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingResultData;
import com.edcs.tds.storm.redis.RedisCacheKey;
import com.edcs.tds.storm.redis.RedisClient;
import com.edcs.tds.storm.scheme.MessageScheme;
import com.edcs.tds.storm.service.DataHandler;
import com.edcs.tds.storm.service.DataService;
import com.edcs.tds.storm.service.MessageRepeatFilter;
import com.edcs.tds.storm.service.SyncResultDataService;
import com.edcs.tds.storm.util.CommonUtil;
import com.edcs.tds.storm.util.StormBeanFactory;
import com.google.common.collect.Lists;

import groovy.lang.Binding;

public class CalcBolt extends BaseRichBolt {

	private static final Logger logger = LoggerFactory.getLogger(CalcBolt.class);

	private static final long serialVersionUID = 5443752882009732861L;

	protected OutputCollector collector;
	protected String topologyName;
	protected StormBeanFactory beanFactory;
	protected ScriptExecutor scriptExecutor;
	protected DataService dataService;
	protected ExecutorService cachedThreadPool;
	protected SyncResultDataService syncResultDataService;
	protected MessageRepeatFilter messageRepeatFilter;
	protected RedisClient redisClient;

	private MetricRegistry metricRegistry;
	private Slf4jReporter metricReporter;
	private Timer timmerInitRequestMessage;
	private Timer timmerRepeatFilter;
	private Timer timmerUpdateStepLogicNumber;
	private Timer timmerGetMasterData;
	private Timer timmerBusinessCycle;
	private Timer timmerUpdateTimeConstantP;
	private Timer timmerInitShellContext;
	private Timer timmerRuleCalc;
	private Timer timmersaveresult;

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.topologyName = (String) stormConf.get(Config.TOPOLOGY_NAME);
		this.beanFactory = new StormBeanFactory(stormConf);
		this.redisClient = beanFactory.getBean(RedisClient.class);
		this.scriptExecutor = beanFactory.getBean(ScriptExecutor.class);
		this.messageRepeatFilter = beanFactory.getBean(MessageRepeatFilter.class);
		this.dataService = beanFactory.getBean(DataService.class);
		this.cachedThreadPool = beanFactory.getBean(ThreadPoolExecutor.class);
		this.syncResultDataService = beanFactory.getBean(SyncResultDataService.class);
		DataCacheService.createCache();
		FilterMappingService.createCache();
		ScriptCacheService.createCache();
		FilterCacheService.createCache();

		this.metricRegistry = new MetricRegistry();
		this.timmerInitRequestMessage = metricRegistry.timer("InitRequestMessage");
		this.timmerRepeatFilter = metricRegistry.timer("repeatFilter");
		this.timmerUpdateStepLogicNumber = metricRegistry.timer("updateStepLogicNumber");
		this.timmerGetMasterData = metricRegistry.timer("GetMasterData");
		this.timmerBusinessCycle = metricRegistry.timer("businessCycle");
		this.timmerUpdateTimeConstantP = metricRegistry.timer("updateTimeConstantP");
		this.timmerInitShellContext = metricRegistry.timer("timmerInitShellContext");
		this.timmerRuleCalc = metricRegistry.timer("timmerRuleCalc");
		this.timmersaveresult = metricRegistry.timer("timmersaveresult");
		this.metricReporter = Slf4jReporter.forRegistry(metricRegistry).outputTo(logger)
				.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
		metricReporter.start(1, TimeUnit.MINUTES);
		logger.info("The {} blot is prepared..", topologyName);
	}

	@Override
	public void execute(Tuple input) {
		try {
			process(input);
		} finally {
			collector.ack(input);
		}
	}

	private void process(Tuple input) {
		RuleCalc calc = new RuleCalc();
		ExecuteContext executeContext = new ExecuteContext();
		Binding shellContext = new Binding();
		List<TestingResultData> resultDatas = Lists.newArrayList();
		boolean isRepeated = false;
		boolean isFilterMapping = false;
		TestingMessage testingMessage = null;
		MasterData masterData = null;
		try {
			logger.info("Start parsing request data...");
			Timer.Context timeInitRequestMessage = timmerInitRequestMessage.time();
			try {
				// 解析请求数据
				testingMessage = DataHandler.initRequestMessage(input, dataService, redisClient);
			} catch (Exception e) {
				logger.error("get testMsg error,testMsg={},error={}",
						input.getStringByField(MessageScheme.SCHEME_MESSAGE_VALUE), e);
			}
			timeInitRequestMessage.stop();
			if (testingMessage == null) {
				isFilterMapping = true;
				return;
			}
			Timer.Context repeatFilter = timmerRepeatFilter.time();
			// 消息重复消费过滤
			isRepeated = repeatFilter(testingMessage.getMessageId());
			if (!isRepeated) {
				logger.warn("testing message is repeat, messageId is {}", testingMessage.getMessageId());
				isFilterMapping = true;
				return;
			}
			repeatFilter.stop();
			Timer.Context timeUpdateStepLogicNumber = timmerUpdateStepLogicNumber.time();
			// 维护工步的逻辑序号
			dataService.updateStepLogicNumber(testingMessage);
			timeUpdateStepLogicNumber.stop();

			// 如果是工步的起始，则记录下对应的sequenceId，便于找到该工步的第一条数据
			String stepRelativeSeqKey = StringUtils
					.join(testingMessage.getRemark() + ":" + testingMessage.getStepLogicNumber());
			if (testingMessage.isStartStep()) {
				FilterCacheService.setFirstSeqCache(stepRelativeSeqKey, testingMessage.getSequenceId());
			}
			// 如果是流程的保护或者暂停，则记录下对应的sequenceId，便于找到该保护或者暂停之后的两条数据
			String continueRelativeSeqKey = StringUtils.join("TES:" + testingMessage.getRemark());
			if (testingMessage.isProtectMsg() || testingMessage.isSuspendMsg()) {
				FilterCacheService.setFirstSeqCache(continueRelativeSeqKey, testingMessage.getSequenceId());
			}
			if (testingMessage.isOverStep()) {
				FilterCacheService.deleteSeqCache(stepRelativeSeqKey);
			}
			// 获取该条测试数据对应的主数据信息
			Timer.Context timeGetMasterData = timmerGetMasterData.time();
			masterData = getMasterData(testingMessage.getRemark());
			timeGetMasterData.stop();
			if (masterData != null) {
				// 维护测试数据中的业务循环号（businessCycle）
				Timer.Context timeBusinessCycle = timmerBusinessCycle.time();
				dataService.updateBusinessCycle(testingMessage, masterData);
				timeBusinessCycle.stop();
				// 维护t恒压的值
				Timer.Context timeUpdateTimeConstantP = timmerUpdateTimeConstantP.time();
				if (SystemConfig.CONSTANT_CURRENT_VOLTAGE_CHARGE.equals(testingMessage.getStepName())) {
					dataService.updateTimeConstantP(testingMessage, masterData);
				}
				timeUpdateTimeConstantP.stop();
				// 当前工步的第i条记录数据
				BigDecimal stepRelativeSeq = FilterCacheService.getRelativeSeq(stepRelativeSeqKey,
						testingMessage.getSequenceId());
				if (stepRelativeSeq != null) {
					stepRelativeSeq = stepRelativeSeq.add(new BigDecimal(1));
				}
				// 接续(保护后接续或停止后接续等)的第i条记录数据
				BigDecimal continueRelativeSeq = null;
				if (!(testingMessage.isProtectMsg() || testingMessage.isSuspendMsg())) {
					continueRelativeSeq = FilterCacheService.getRelativeSeq(continueRelativeSeqKey,
							testingMessage.getSequenceId());
				}
				// 初始化 ShellContext
				Timer.Context timeInitShellContext = timmerInitShellContext.time();
				shellContext = DataHandler.initShellContext(testingMessage, dataService, shellContext, masterData);
				shellContext.setProperty(SystemConfig.STEPRELATIVESEQ, stepRelativeSeq);
				shellContext.setProperty(SystemConfig.PROTECTRELATIVESEQ, continueRelativeSeq);
				timeInitShellContext.stop();
				// 核心计算
				Timer.Context timeRuleCalc = timmerRuleCalc.time();
				resultDatas = calc.testingRuleCalc(scriptExecutor, testingMessage, shellContext, masterData,
						cachedThreadPool, redisClient);
				timeRuleCalc.stop();
			}
			// 告知redis此流程已经结束
			if (testingMessage.isOverMsg()) {
				// 清除维护的BusinessCycle
				String businessCycleKey = RedisCacheKey.getCycleQueue(testingMessage.getRemark());
				// 清除维护的StepLogicNumber
				String stepLogicNumberKey = RedisCacheKey.getStepLogicQueue(testingMessage.getRemark());
				// 清除维护的主数据对应的版本信息
				String versionKey = CommonUtil.underlineLink().join("MD", testingMessage.getRemark(), "VERSION");
				String listInfoStateKey = RedisCacheKey.getAlertListInfoState(testingMessage.getRemark());
				String listInfoHandleKey = RedisCacheKey.getListInfoHandle(testingMessage.getRemark());
				/* 清除流程每个场景的预警次数--start */
				String alterNumberKeyV = RedisCacheKey.getAlterNumber(testingMessage.getRemark(),
						testingMessage.getResourceId(), testingMessage.getChannelId(), SystemConfig.VLOTAGESCENENAME);
				String alterNumberKeyI = RedisCacheKey.getAlterNumber(testingMessage.getRemark(),
						testingMessage.getResourceId(), testingMessage.getChannelId(), SystemConfig.CURRENTSCENENAME);
				String alterNumberKeyD = RedisCacheKey.getAlterNumber(testingMessage.getRemark(),
						testingMessage.getResourceId(), testingMessage.getChannelId(), SystemConfig.TIMESCENENAME);
				String alterNumberKeyC = RedisCacheKey.getAlterNumber(testingMessage.getRemark(),
						testingMessage.getResourceId(), testingMessage.getChannelId(), SystemConfig.CAPACITYSCENENAME);
				/* 清除流程每个场景的预警次数--end */

				// 清除内存中缓存的主数据信息
				DataCacheService.removeMasterData(testingMessage.getRemark());
				// 清除内存中缓存的该条主数据对应的脚本信息
				ScriptCacheService.cleanInvalidCache(testingMessage.getRemark());
				// 清除每个工步的第一条数据的sequenceId的缓存数据。
				FilterCacheService.deleteSeqCache(stepRelativeSeqKey);
				FilterCacheService.deleteSeqCache(continueRelativeSeqKey);
				// 清除cacheService中这个流程的缓存脚本信息
				redisClient.del(businessCycleKey, stepLogicNumberKey, versionKey, listInfoStateKey, listInfoHandleKey,
						alterNumberKeyV, alterNumberKeyI, alterNumberKeyD, alterNumberKeyC);
			}
		} catch (Exception e) {
			logger.error("core calc error:", e);
		} finally {
			if (!isFilterMapping) {
				shellContext = null;
				executeContext.setMessage(testingMessage);
				executeContext.setMasterData(masterData);
				executeContext.setResultDatas(resultDatas);
				dataService.insertTestMsgToRedis(testingMessage);// 将测试数据写入到redis
				if (resultDatas.size() < 1) {
					finalProccess(executeContext);
				}
				if (testingMessage != null) {
					try {
						Timer.Context timersaveresult = timmersaveresult.time();
						syncResultDataService.save(executeContext);
						timersaveresult.stop();
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}
	}

	public MasterData getMasterData(String remark) throws ExecutionException {
		MasterData masterData = null;
		String versionKey = CommonUtil.underlineLink().join("MD", remark, "VERSION");
		String version = null;
		version = redisClient.get(versionKey, SystemConfig.OVERTIME);
		logger.info("get master data version from redis, key:{}, version:{}.", versionKey, version);
		masterData = DataCacheService.getMasterData(remark, version);
		if (masterData == null) {
			logger.info("no master data of local cache, loading from hana.. remark:{}.", remark);
			masterData = dataService.getMasterDataInfo(remark);
			if (masterData == null) {
				return null;
			}
			if (StringUtils.isEmpty(version)) {
				version = redisClient.set(versionKey, String.valueOf(System.currentTimeMillis()), null);
				logger.warn("update master data version, key:{}, version:{}.", versionKey, version);
			}
			DataCacheService.addMasterData(remark, masterData, version);
		}
		return masterData;
	}

	public void finalProccess(ExecuteContext context) {
		List<TestingResultData> listResult = context.getResultDatas();
		try {
			MasterData masterData = context.getMasterData();
			TestingMessage testingMessage = context.getMessage();

			TestingResultData testingResultData = new TestingResultData();
			testingResultData.setTestingMessage(testingMessage);
			testingResultData.setOriginalProcessDataBO(context.generateHandle());
			if (masterData != null) {// 表明这条测试数据一个预警信息都没有，但是主数据存在，那么我们要产生一个result对象来存放原始测试数据信息
				String site = masterData.getSite();
				testingResultData.setSite(site);
				testingResultData.setRootRemark(masterData.getRootRemark());
				testingResultData.setProcessDataBO(masterData.getHandle());
				testingResultData.setErpResourceBO(
						CommonUtil.commaLink().join("ErpResourceBO:" + site, testingMessage.getResourceId()));
				testingResultData.setIsContainMainData("1");// 表示匹配上主数据
			}
			if (masterData == null) {// 表明这条测试数据一个预警信息都没有，但是主数据不存在，那么我们要产生一个result对象来存放原始测试数据信息
				String site = SystemConfig.SITE;
				testingResultData.setSite(site);
				testingResultData.setIsContainMainData("2");// 表示没有匹配上主数据
			}
			listResult.add(testingResultData);
		} catch (Exception e) {
			logger.error("final proccess error:", e);
		} finally {
			context.setResultDatas(listResult);
		}
	}

	public boolean repeatFilter(String messageId) {
		return messageRepeatFilter.filter(messageId);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// NOTHING TO DO
		// declarer.declare(new Fields(MessageScheme.SCHEME_MESSAGE_KEY,
		// MessageScheme.SCHEME_MESSAGE_VALUE));
	}
}
