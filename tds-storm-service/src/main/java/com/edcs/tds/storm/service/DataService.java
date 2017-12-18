package com.edcs.tds.storm.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.storm.shade.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.db.HanaDataHandler;
import com.edcs.tds.storm.model.MDStepInfo;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.redis.RedisCacheKey;
import com.edcs.tds.storm.redis.RedisClient;
import com.edcs.tds.storm.util.DataSerializer;

import redis.clients.util.SafeEncoder;

public class DataService {

    final static Logger logger = LoggerFactory.getLogger(DataService.class);

    private HanaDataHandler hanaDataHandler;

    public void setHanaDataHandler(HanaDataHandler hanaDataHandler) {
        this.hanaDataHandler = hanaDataHandler;
    }

    private RedisClient redisClient;

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * 根据流程号和序号来获取一条测试数据的上一条数据信息 先到redis中去找
     *
     * @param testingMessage
     * @param i
     *            表示获取这条测试数据的上几条测试数据
     * @return
     */
    public TestingMessage getUpTestingMsg(TestingMessage testingMessage, int i) {
        TestingMessage testingMsg = null;// 用于返回值
        // 先从redis读取数据，如果读取不到，则再去hana中读取
        int seqId = testingMessage.getSequenceId() - i;
        try {
            byte[] data = null;
            String key = RedisCacheKey.getTempResultData(testingMessage.getRemark(), String.valueOf((seqId)));
            data = redisClient.get(SafeEncoder.encode(key), null);
            if (data != null) {
                testingMsg = (TestingMessage) DataSerializer.asObjectForDefault(data);
            } else {
                // 如果读取不到，则再去hana中读取
                testingMsg = hanaDataHandler.queryTestingRecord(HanaDataHandler.QUERY_TESTING_LAST_RECORD_SQL,
                        testingMessage.getRemark(), seqId);
            }
        } catch (Exception e) {
            logger.error("getUpTestingMsg error ,remark={},sequenceId={}", testingMessage.getRemark(),
                    testingMessage.getSequenceId(), e);
        }
        return testingMsg;
    }

    /**
     * 获取一个循环中一个工步中的第一条数据
     *
     * @param testingMessage
     * @param cacheService
     * @return
     */
    public TestingMessage getFirstTestingMsg(TestingMessage testingMessage) {
        TestingMessage testingMsgResult = null;// 用于返回值
        // 先从redis读取数据，如果读取不到，则再去hana中读取
        try {
            int sequenceId = testingMessage.getSequenceId();
            if (sequenceId > 1) {
                for (int i = sequenceId - 1; i > 0; i--) {
                    String key = RedisCacheKey.getTempResultData(testingMessage.getRemark(), String.valueOf(i));
                    byte[] data = redisClient.get(SafeEncoder.encode(key), null);
                    if (data == null) {
                        break;
                    }
                    TestingMessage testingMsg = (TestingMessage) DataSerializer.asObjectForDefault(data);

                    int businessCycle = testingMsg.getBusinessCycle();// 业务循环号
                    // 如果这条测试数据是当前测试数据的同一个循环的同一个工步的第一条测试数据，则取出。
                    if (businessCycle == testingMessage.getBusinessCycle()
                            && testingMsg.getStepLogicNumber() == testingMessage.getStepLogicNumber()
                            && testingMsg.isStartStep()) {
                        testingMsgResult = testingMsg;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getFirstTestingMsg from redis error ,remark={},sequenceId={}", testingMessage.getRemark(),
                    testingMessage.getSequenceId(), e);
        }
        // 如果读取不到，则再去hana中读取
        if (testingMsgResult == null) {
            try {
                testingMsgResult = hanaDataHandler.queryTestingRecord(
                        HanaDataHandler.QUERY_TESTING_STEP_FIRST_RECORD_SQL, testingMessage.getRemark(),
                        testingMessage.getBusinessCycle(), testingMessage.getStepLogicNumber());
            } catch (Exception e) {
                logger.error("getFirstTestingMsg from hana error ,remark={},sequenceId={}", testingMessage.getRemark(),
                        testingMessage.getSequenceId(), e);
            }
        }
        return testingMsgResult;
    }

    /**
     *
     * @param testingMessage
     * @param masterData
     * @param i
     * @return
     */
    public Map<String,String> getUpStepInfo(TestingMessage testingMessage, MasterData masterData){
        Map<String,String> map = Maps.newHashMap();
        int i = testingMessage.getStepLogicNumber();
        int stepId = testingMessage.getStepId();
        try {
            String stepInfoKey = RedisCacheKey.getUpStepInfoKey(testingMessage.getRemark(), i);
            if (null == redisClient.get(stepInfoKey, 60 * 60 * 24)){
                String svCurrent = String.valueOf(masterData.getStepData().get(stepId).getSvCurrent());
                //value类型:         stepName:svCurrent
                if (StringUtils.isBlank(svCurrent))
                    svCurrent = "";
                String value = RedisCacheKey.getUpStepInfoValue(testingMessage.getStepName(), svCurrent);
                redisClient.set(stepInfoKey, value, 60 * 60 * 24);
            }
        } catch (Exception e) {
            logger.error("getUpStepInfo error : "+e);
        }
        try {
            if (1 != i) {
                String upStepInfoKey = RedisCacheKey.getUpStepInfoKey(testingMessage.getRemark(), i - 1);
                String upStepInfo = redisClient.get(upStepInfoKey, 60 * 60 * 24);
                String stepName = null;
                String svCurrent = null;
                if (null != upStepInfo){
                    stepName = upStepInfo.split(":")[0];
                    svCurrent = upStepInfo.split(":")[1];
                    map.put("stepName",stepName);
                    map.put("svCurrent",svCurrent);
                }

            }
        } catch (Exception e) {
            logger.error("get redis upStepInfo error : "+e);
        }
        //如果取不到就到hana里去
        if (0 == map.size()){
            try {
                map = hanaDataHandler.queryUpStepInfo(HanaDataHandler.QUERY_UPSTEP_INFO_SQL, testingMessage.getRemark(), testingMessage.getStepLogicNumber(), testingMessage.getRemark(), testingMessage.getStepId());
            } catch (SQLException e) {
                logger.error("get upStepInfo from hana error" , e);
            }
        }

        return map;
    }
    /**
     * 获取一个流程中的上一个工步的最后一条数据（先到redis中获取，如果获取不到就到hana中获取）
     *
     * @param testingMessage
     * @param i（表明上几个工步）
     * @param cacheService
     * @return
     */
    public TestingMessage getUpStepTestingMsg(TestingMessage testingMessage, int i) {
        TestingMessage testingMsgReturn = null;
        try {
            int sequenceId = testingMessage.getSequenceId();
            if (sequenceId > 1) {
                for (int j = sequenceId - 1; j > 0; j--) {
                    String key = RedisCacheKey.getTempResultData(testingMessage.getRemark(), String.valueOf(j));
                    byte[] data = redisClient.get(SafeEncoder.encode(key), null);
                    if (data == null) {
                        break;
                    }
                    TestingMessage testingMsg = (TestingMessage) DataSerializer.asObjectForDefault(data);

                    int stepLogicNumber = testingMsg.getStepLogicNumber();
                    // 如果这条测试数据是当前测试数据的上i个工步中的测试数据，且是上i个工步中的最后一条数据，则取出。
                    if (stepLogicNumber == testingMessage.getStepLogicNumber() - i && testingMsg.isOverStep()) {
                        testingMsgReturn = testingMsg;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getUpStepTestingMsg from redis error ,remark={},sequenceId={}", testingMessage.getRemark(),
                    testingMessage.getSequenceId(), e);
        }
        // redis 中没有找到，则到hana中找
        if (testingMsgReturn == null) {
            try {
                testingMsgReturn = hanaDataHandler.queryTestingRecord(
                        HanaDataHandler.QUERY_TESTING_STEP_LAST_RECORD_SQL, testingMessage.getRemark(),
                        testingMessage.getStepLogicNumber() - i);
            } catch (Exception e) {
                logger.error("getUpStepTestingMsg from hana error ,remark={},sequenceId={}", testingMessage.getRemark(),
                        testingMessage.getSequenceId(), e);
            }
        }

        return testingMsgReturn;
    }

    /**
     * 根据rootRemark去HANA中查询这条和rootRemark对应的remark的流程数据。
     * （查找这个和rootRemark相同的remark 的流程循环次数）
     *
     * @param remark
     * @return
     */
    public int getCycleNum(String rootRemark) {
        int cycleNum = 0;
        try {
            cycleNum = hanaDataHandler.queryCycleNumber(rootRemark);
        } catch (Exception e) {
            logger.error("query cycle number from hana was error:", e);
        }
        return cycleNum;
    }

    public MasterData getMasterDataInfo(String remark) {
        MasterData masterData = null;
        try {
            masterData = hanaDataHandler.queryMasterDataRecord(remark);
            if (masterData == null) {
                logger.warn("can not find master data from hana database, remark:" + remark);
                return null;
            }
            Map<Integer, MDStepInfo> stepData = hanaDataHandler.queryMasterDataStepRecord(remark);
            masterData.setStepData(stepData);
        } catch (Exception e) {
            logger.error("query master data from hana was error:", e);
        }

        return masterData;
    }

    /**
     * 将测试数据放入到redis中用于取上一条等
     */
    public void insertTestMsgToRedis(TestingMessage testingMessage) {
        try {
            String key = RedisCacheKey.getTempResultData(testingMessage.getRemark(),
                    String.valueOf(testingMessage.getSequenceId()));
            redisClient.set(SafeEncoder.encode(key), DataSerializer.asByteArrayForDefault(testingMessage),
                    SafeEncoder.encode("NX"), SafeEncoder.encode("EX"), 60 * 5);
        } catch (Exception e) {
            logger.error("save testing result to redis was error:", e);
        }
    }

    /**
     * 维护测试数据的sequenceId
     *
     * @param remark
     * @param nowSequenceNum
     * @return
     */
/*	public Integer updateSequenceId(String remark, int nowSequenceNum) {
		int newSequenceNum = 1;
		Jedis conn = null;
		try {
			String key = RedisCacheKey.getSequenceQueue(remark);
			conn = redisClient.getConn();
			String num = conn.get(key);
			if (StringUtils.isNotEmpty(num)) {
				int lastSequenceNum = Integer.valueOf(num);
				if (nowSequenceNum <= lastSequenceNum) {
					newSequenceNum = lastSequenceNum + 1;
				} else {
					newSequenceNum = nowSequenceNum;
				}
			}
			conn.set(key, String.valueOf(newSequenceNum));
		} finally {
			redisClient.closeQuietly(conn);
		}
		return newSequenceNum;
	}*/

    /**
     * 维护测试数据中的业务循环号（businessCycle）
     *
     * 1.首先判断这条测试数据是否是某个测试流程数据中的第一条数据。 如果是第一条数据，则需要判断这条数据对应的流程对应的rootRemark 有没有值
     * 如果有则需要查询 rootRemark 对应的流程 的循环次数 将上一个的循环次数+1 作为当前循环的起始循环号。
     * 2.查询这条测试数据对应的公布的 IS_CYCLE_SIGNAL_STEP 如果为true 则
     * 根据流程号+测试编号来查询这条测试数据的上一条测试数据的公布类型，如果两条数据的公布类型不同，则业务循环号需要+1 反之则不需要+1. 如果
     * IS_CYCLE_SIGNAL_STEP 为false 则直接跳过，业务循环号不需要+1.
     *
     * @param testingMessage
     * @param masterData
     */
    public void updateBusinessCycle(TestingMessage testingMessage, MasterData masterData) throws Exception {
		/*
		 * 到CacheService中获取这个测试数据对应的流程信息
		 * （也可以从redis里面获取，但是考虑到性能问题，就让CacheService中缓存一份redis所有流程的信息。这样就是牺牲内存换性能）
		 */
        String key = RedisCacheKey.getCycleQueue(testingMessage.getRemark());
        String businessCycle = redisClient.get(key, SystemConfig.OVERTIME);
        String midComeKey = RedisCacheKey.getIsMidComeKey(testingMessage.getRemark());
        if (StringUtils.isEmpty(businessCycle)) {// 表明这个测试数据是某个流程的起始数据
            //redis找不到再去hana查
            String maxBusinessCycle = hanaDataHandler.queryMaxBusinessCycle(testingMessage.getRemark());
            int newCycleNum = 1;
            if (StringUtils.isBlank(maxBusinessCycle)) {
                if (masterData.getCycleNumber() != 0) {
                    newCycleNum = masterData.getCycleNumber();
                    // 如果中途接入,设置一个标识,用于判断是否跳过第一次循环
                    redisClient.set(midComeKey, String.valueOf(1), SystemConfig.OVERTIME);
                } else {
                    String rootRemark = masterData.getRootRemark();// 获取该条测试数据对应流程的rootremark的值
                    if (StringUtils.isNotEmpty(rootRemark)) {// 如果rootRemark有值
                        newCycleNum = getCycleNum(rootRemark) + 1;
                    }
                }
                testingMessage.setBusinessCycle(newCycleNum);
                redisClient.set(key, String.valueOf(newCycleNum), null);
            }else {
                businessCycle = maxBusinessCycle;
                logger.info("get maxBusinessCycle from Hana");
                int num;
                // 查看这条测试数据对应的工步的IS_CYCLE_SIGNAL_STEP
                // 是否为true，如果为true则进行下面的操作，如果为false，则什么都不做
                MDStepInfo mdStepInfo = masterData.getStepData().get(testingMessage.getStepId());// 找到这个测试信息对应的工步信息
                boolean isCycleSignalStep = mdStepInfo.isCycleSignalStep();
                // 如果该测试数据对应的工步的 循环标志为 true且是一个工步的第一条数据
                TestingMessage upTestingMessage = this.getUpTestingMsg(testingMessage,1);
                if ((testingMessage.getStepId() != upTestingMessage.getStepId()) && isCycleSignalStep) {

                    String cycleKey = RedisCacheKey.getCycleKey(testingMessage.getRemark() , testingMessage.getStepId());
                    if (!redisClient.exists(cycleKey,SystemConfig.OVERTIME) && !redisClient.exists(midComeKey,SystemConfig.OVERTIME)) {
                        redisClient.set(cycleKey, String.valueOf(1), SystemConfig.OVERTIME);
                        num = Integer.parseInt(businessCycle);
                    } else {
                        num = Integer.parseInt(businessCycle) + 1;
                    }
                    testingMessage.setBusinessCycle(num);
                    redisClient.set(key, String.valueOf(num), null);
                } else {
                    testingMessage.setBusinessCycle(Integer.parseInt(businessCycle));
                }
            }

        } else {
            int num;
            // 查看这条测试数据对应的工步的IS_CYCLE_SIGNAL_STEP
            // 是否为true，如果为true则进行下面的操作，如果为false，则什么都不做
            MDStepInfo mdStepInfo = masterData.getStepData().get(testingMessage.getStepId());// 找到这个测试信息对应的工步信息
            boolean isCycleSignalStep = mdStepInfo.isCycleSignalStep();
            // 如果该测试数据对应的工步的 循环标志为 true且是一个工步的第一条数据
            TestingMessage upTestingMessage = this.getUpTestingMsg(testingMessage,1);
            if ((testingMessage.getStepId() != upTestingMessage.getStepId()) && isCycleSignalStep) {
                String cycleKey = RedisCacheKey.getCycleKey(testingMessage.getRemark() , testingMessage.getStepId());
                if (!redisClient.exists(cycleKey,SystemConfig.OVERTIME) && !redisClient.exists(midComeKey,SystemConfig.OVERTIME)) {
                    redisClient.set(cycleKey, String.valueOf(1), SystemConfig.OVERTIME);
                    num = Integer.parseInt(businessCycle);
                } else {
                    num = Integer.parseInt(businessCycle) + 1;
                }
                testingMessage.setBusinessCycle(num);
                redisClient.set(key, String.valueOf(num), null);
            } else {
                testingMessage.setBusinessCycle(Integer.parseInt(businessCycle));
            }
        }
    }

    /**
     * 维护工步的逻辑序号
     *
     * @param testingMessage
     */
    public void updateStepLogicNumber(TestingMessage testingMessage) throws Exception {
        String key = RedisCacheKey.getStepLogicQueue(testingMessage.getRemark());
        Integer newStepLogicNum = 1;
        String maxLogicNumber = null;
        String oldStepLogicNumber = redisClient.get(key, SystemConfig.OVERTIME);
        if (StringUtils.isBlank(oldStepLogicNumber)){
            maxLogicNumber = hanaDataHandler.queryMaxLogicNumber(testingMessage.getRemark());
            if (StringUtils.isNotBlank(maxLogicNumber)){
                TestingMessage upTestingMessage = this.getUpTestingMsg(testingMessage,1);
                if (null != upTestingMessage && upTestingMessage.getStepId() != testingMessage.getStepId()){
                    newStepLogicNum = Integer.parseInt(maxLogicNumber)+1;
                }else {
                    newStepLogicNum = Integer.parseInt(maxLogicNumber);
                }
            }
        }else {
            TestingMessage upTestingMessage = this.getUpTestingMsg(testingMessage,1);
            if (null != upTestingMessage && upTestingMessage.getStepId() != testingMessage.getStepId()){
                newStepLogicNum = Integer.parseInt(oldStepLogicNumber)+1;
            }else {
                newStepLogicNum = Integer.parseInt(oldStepLogicNumber);
            }
        }
        redisClient.set(key, newStepLogicNum.toString(), null);
        testingMessage.setStepLogicNumber(newStepLogicNum);
        logger.info("updated testing step logic number, remark:{}, new logic number:{}.", testingMessage.getRemark(),
                testingMessage.getStepLogicNumber());
    }

    /**
     * 维护t恒压
     *
     * @param testingMessage
     * @param masterData
     * @return
     */
    public void updateTimeConstantP(TestingMessage testingMessage, MasterData masterData) {
        String key = RedisCacheKey.getTimeConstantPKey(testingMessage.getRemark());
        String time = redisClient.get(key, SystemConfig.OVERTIME);
        if (StringUtils.isEmpty(time)) {
            MDStepInfo mdStepInfo = masterData.getStepData().get(testingMessage.getStepId());
            double range = 0.005;
            //svvoltage=3.65
            int small = testingMessage.getPvVoltage()
                    .compareTo(mdStepInfo.getSvVoltage().subtract(new BigDecimal(range)));
            int big = testingMessage.getPvVoltage().compareTo(mdStepInfo.getSvVoltage().add(new BigDecimal(range)));
            if (small >= 0 && big <= 0) {
                redisClient.set(key, testingMessage.getTestTimeDuration().toString(), null);
            }
            logger.info("updated testing T constant pressure value, new value:{}, remark:{}, step id:{}.",
                    testingMessage.getTestTimeDuration(), testingMessage.getRemark(), testingMessage.getStepId());
        } else {
            BigDecimal ti = new BigDecimal(time);
            testingMessage.setTimeConstantP(testingMessage.getTestTimeDuration().subtract(ti));
            if (testingMessage.isOverStep()) {// 如果是该工步的最后一条数据，则用过之后需要清除
                redisClient.del(key);
            }
            logger.info("remove testing T constant pressure value,value:{}, remark:{}, step id:{}.",
                    testingMessage.getTestTimeDuration(), testingMessage.getRemark(), testingMessage.getStepId());
        }
    }
}
