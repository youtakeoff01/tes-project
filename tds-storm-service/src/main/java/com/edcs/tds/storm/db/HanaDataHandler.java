package com.edcs.tds.storm.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.edcs.tds.storm.dao.domain.AlertInfoModel;
import com.edcs.tds.storm.dao.domain.AlertListInfoModel;
import com.edcs.tds.storm.dao.domain.OriginalDataModel;
import com.edcs.tds.storm.dao.domain.SubChannelModel;
import com.edcs.tds.storm.dao.domain.ZipDataModel;
import com.edcs.tds.storm.model.MDStepInfo;
import com.edcs.tds.storm.model.MasterData;
import com.edcs.tds.storm.model.SystemConfig;
import com.edcs.tds.storm.model.TestingMessage;
import com.edcs.tds.storm.model.TestingResultData;
import com.edcs.tds.storm.redis.RedisCacheKey;
import com.edcs.tds.storm.redis.RedisClient;
import com.google.common.collect.Maps;

/**
 * All HANA data DDL operations
 */
public class HanaDataHandler {

	private DatabaseHelper databaseHelper;
	private RedisClient redisClient;

	public void setDatabaseHelper(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void setRedisClient(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	public static String QUERY_UPSTEP_INFO_SQL = "select STEP_NAME,SV_CURRENT from (select STEP_NAME from tx_original_process_data where remark = ? and STEP_LOGIC_NUMBER = ? limit 1)a , (select SV_CURRENT from MD_STEP_INFO where remark = ? and STEP_ID = ?) b";

	public static String QUERY_TESTING_LAST_RECORD_SQL = "select REMARK,SFC,RESOURCE_ID,CHANNEL_ID,SEQUENCE_ID,"
			+ "CYCLE,STEP_ID,STEP_NAME,TEST_TIME_DURATION,TIMESTAMP,SV_IC_RANGE,"
			+ "SV_IV_RANGE,PV_VOLTAGE,PV_CURRENT,PV_IR,PV_TEMPERATURE,PV_CHARGE_CAPACITY,"
			+ "PV_DISCHARGE_CAPACITY,PV_CHARGE_ENERGY,PV_DISCHARGE_ENERGY,ST_BUSINESS_CYCLE "
			+ "from tx_original_process_data where REMARK= ? and SEQUENCE_ID = ?";

	public static String QUERY_TESTING_STEP_FIRST_RECORD_SQL = "select REMARK,SFC,RESOURCE_ID,CHANNEL_ID,SEQUENCE_ID,"
			+ "CYCLE,STEP_ID,STEP_NAME,TEST_TIME_DURATION,TIMESTAMP,SV_IC_RANGE,"
			+ "SV_IV_RANGE,PV_VOLTAGE,PV_CURRENT,PV_IR,PV_TEMPERATURE,PV_CHARGE_CAPACITY,"
			+ "PV_DISCHARGE_CAPACITY,PV_CHARGE_ENERGY,PV_DISCHARGE_ENERGY,ST_BUSINESS_CYCLE "
			+ "from tx_original_process_data where REMARK= ? and ST_BUSINESS_CYCLE = ? and step_logic_number = ? and PV_DATA_FLAG = 89";

	public static String QUERY_TESTING_STEP_LAST_RECORD_SQL = "select REMARK,SFC,RESOURCE_ID,CHANNEL_ID,SEQUENCE_ID,"
			+ "CYCLE,STEP_ID,STEP_NAME,TEST_TIME_DURATION,TIMESTAMP,SV_IC_RANGE,"
			+ "SV_IV_RANGE,PV_VOLTAGE,PV_CURRENT,PV_IR,PV_TEMPERATURE,PV_CHARGE_CAPACITY,"
			+ "PV_DISCHARGE_CAPACITY,PV_CHARGE_ENERGY,PV_DISCHARGE_ENERGY,ST_BUSINESS_CYCLE "
			+ "from tx_original_process_data where REMARK= ? and step_logic_number = ? and PV_DATA_FLAG = 88";

	public static String QUERY_CYCLE_NUMBER_SQL = "select max(total_cycle_num) from MD_PROCESS_INFO where remark = ? or root_remark = ?";

	private static String stepsql = "SELECT HANDLE,SITE,REMARK,STEP_ID,STEP_NAME,SCRIPT_CURRENT,SCRIPT_VOLTAGE,SCRIPT_TEMPERATURE,SCRIPT_TIME,SCRIPT_CAPACITY,SCRIPT_ENERGY,IS_CYCLE_SIGNAL_STEP,DELTA_VOLTAGE,SV_POWER,SV_IR,SV_VOLTAGE,SV_CAPACITY,SV_CURRENT,SV_STEP_END_CAPACITY,SV_STEP_END_CURRENT,SV_STEP_END_VOLTAGE,SV_STEP_END_TEMPERATURE,SV_ENERGY,SV_TEMPERATURE,SV_TIME,CYCLE_COUNT,CONDITION_TYPE,CONDITION_OPERATIONAL_CHARACTER,CONDITION_VALUE,GOTO_STEP,START_STEP,START_SOC,SOC_INCREMENT,END_SOC FROM MD_STEP_INFO WHERE remark = ?";
	private static String mastersql = "SELECT HANDLE,SITE,REMARK,PROCESS_ID,TEST_REQUEST_ID,GROUP_NAME,SFC,ENGINEER,PACKAGE_ID,PROJECT,DESCRIPTION,SV_MODEL,SV_CAPACITY_VALUE,FIXTURE_TYPE,SV_INIT_FIXTURE_FORCE,SV_INIT_WEIGHT,SV_INIT_VOLUME,SV_INIT_IR,SV_INIT_OCV,SV_INIT_THICKNESS,DAYS_OR_CYCLE,SV_LOWER_U,SV_UPPER_U,SV_TEMPERATURE,SV_TIME_DURATION,SV_CHARGE_CURRENT,SV_DISCHARGE_CURRENT,SV_CHARGE_VOLTAGE,SV_DISCHARGE_VOLTAGE,SV_CHARGE_POWER,SV_DISCHARGE_POWER,CYCLE_NUMBER,SOC,PULSE_TIMES,CHARGE_MULTI,DISCHARGE_MULTI,STORE_END_CONDITION,STORE_CROSS_CONDITION,CYCLE_END_CONDITION,CYCLE_CROSS_CONDITION,SIM_END_CONDITION,SIM_CROSS_CONDITION,IS_ISO_DISCHARGE,ISO_DISCHARGE_DURATION,CONSTANT_IR_VALUE,CYCLE_TEMPERATURE,STORE_TEMPERATURE,TX_STATUS,TOTAL_CYCLE_NUM,ROOT_REMARK \n"
			+ "\n from MD_PROCESS_INFO WHERE remark = ?";

	// result to hana
	private static String STATUS = "new";
	private static String listSql = "SELECT HANDLE FROM TX_ALERT_LIST_INFO WHERE  remark=? AND STATUS=? limit 1 ";
	private static String updateSql = "UPDATE MD_PROCESS_INFO SET TX_STATUS=?,TOTAL_CYCLE_NUM=?,MODIFIED_DATE_TIME=?,MODIFIED_USER=? WHERE REMARK=?";

	private static String alertListsql = "insert into TX_ALERT_LIST_INFO(HANDLE,SITE,ALERT_LIST_ID,REMARK,STATUS,FEEDBACK_I,FEEDBACK_U,FEEDBACK_T,FEEDBACK_C,FEEDBACK_D,COMMENTS,CREATED_DATE_TIME,CREATED_USER,MODIFIED_DATE_TIME,MODIFIED_USER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String alertInfosql = "insert into TX_ALERT_INFO(HANDLE,SITE,REMARK,SFC,CATEGORY,ALERT_SEQUENCE_NUMBER,TX_ALERT_LIST_INFO_BO,STATUS,PROCESS_INFO_BO,TIMESTAMP,ERP_RESOURCE_BO,CHANNEL_ID,ALERT_LEVEL,DESCRIPTION,UP_LIMIT,LOW_LIMIT,ORIGINAL_PROCESS_DATA_BO,CREATED_DATE_TIME,CREATED_USER,MODIFIED_DATE_TIME,MODIFIED_USER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String subsql = "insert into TX_ORIGINAL_SUB_CHANNEL_DATA(HANDLE,TX_ORIGINAL_PROCESS_DATA_BO,SUB_CHANNEL_ID,SITE,REMARK,SFC,RESOURCE_ID,CHANNEL_ID,SEQUENCE_ID,CYCLE,STEP_ID,TEST_TIME_DURATION,PV_VOLTAGE,PV_CURRENT,PV_IR,PV_TEMPERATURE,PV_CHARGE_CAPACITY,PV_DISCHARGE_CAPACITY,PV_CHARGE_ENERGY,PV_DISCHARGE_ENERGY,TIMESTAMP,DATA_FLAG,WORK_TYPE,CREATED_DATE_TIME,CREATED_USER,MODIFIED_DATE_TIME,MODIFIED_USER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String procesql = "insert into TX_ORIGINAL_PROCESS_DATA(HANDLE,SITE,REMARK,SFC,RESOURCE_ID,CHANNEL_ID,SEQUENCE_ID,CYCLE,STEP_ID,STEP_NAME,TEST_TIME_DURATION,TIMESTAMP,SV_IC_RANGE,SV_IV_RANGE,PV_VOLTAGE,PV_CURRENT,PV_IR,PV_TEMPERATURE,PV_CHARGE_CAPACITY,PV_DISCHARGE_CAPACITY,PV_CHARGE_ENERGY,PV_DISCHARGE_ENERGY,PV_SUB_CHANNEL_1,PV_SUB_CHANNEL_2,PV_SUB_CHANNEL_3,PV_SUB_CHANNEL_4,PV_SUB_CHANNEL_5,PV_SUB_CHANNEL_6,PV_DATA_FLAG,PV_WORK_TYPE,TX_IS_EXCEPTIONAL,TX_ALERT_CURRENT,TX_ALERT_VOLTAGE,TX_ALERT_TEMPERATURE,TX_ALERT_CAPACITY,TX_ALERT_DURATION,TX_ALERT_CATEGORY1,TX_ALERT_CATEGORY2,TX_ROOT_REMARK,ST_BUSINESS_CYCLE,CREATED_DATE_TIME,CREATED_USER,MODIFIED_DATE_TIME,MODIFIED_USER,STEP_LOGIC_NUMBER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
	private static String zipsql = "insert into TECH_ZIP_STATUS(HANDLE,SITE,REMARK,ST_BUSINESS_CYCLE,STEP_ID,I_STATUS,V_STATUS,T_STATUS,C_STATUS,E_STATUS,CREATED_DATE_TIME,CREATED_USER,MODIFIED_DATE_TIME,MODIFIED_USER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";


	private static String getStepLogicSql="SELECT max(STEP_LOGIC_NUMBER) from TX_ORIGINAL_PROCESS_DATA where remark = ?";
	private static String getBusinessCycle = "SELECT max(ST_BUSINESS_CYCLE) from TX_ORIGINAL_PROCESS_DATA where remark = ?";

	public synchronized boolean setState(String remark, String handle) throws SQLException {
		//key = TES:alertListInfoState:T3-20151217-1142-158858_Cycle_52_C4498-F85_10681
		String key = RedisCacheKey.getAlertListInfoState(remark);
		String key2 = RedisCacheKey.getListInfoHandle(remark);
		boolean boo = false;
		String sta = redisClient.get(key, SystemConfig.OVERTIME);
		if (StringUtils.isEmpty(sta)) {
			redisClient.set(key, "new", null);// 1表示 new
			String handle1 = getStatement(remark);// 去hana中查询
			if (StringUtils.isNotEmpty(handle1)) {
				redisClient.set(key2, handle1, SystemConfig.OVERTIME);
				boo = false;
			} else {
				redisClient.set(key2, handle, SystemConfig.OVERTIME);
				boo = true;
			}
		} else {
			if ("new".equals(sta)) {
				boo = false;
			}
			if ("close".equals(sta)) {
				boo = true;
				redisClient.set(key, "new", null);
				redisClient.set(key2, handle, SystemConfig.OVERTIME);
			}
		}
		return boo;
	}

	public String getHandleForRedis(String remark) {
		//TES:listInfoHandle:T3-20171026-1991-792047_Cycle_8_C0-F100_36983
		String key2 = RedisCacheKey.getListInfoHandle(remark);
		String handle = redisClient.get(key2, SystemConfig.OVERTIME);
		if (StringUtils.isNotEmpty(handle)) {
			return handle;
		}
		return null;
	}
	public Map<String,String> queryUpStepInfo(String sql, Object... params) throws SQLException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		String stepName = null;
		String svCurrent = null;
		Map<String,String> map = org.apache.storm.shade.com.google.common.collect.Maps.newHashMap();
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, sql, params);
			result = statement.executeQuery();
			if (result != null){
				if (result.next()){
					stepName = result.getString(1);
					svCurrent = result.getString(2);
				}
				map.put("stepName",stepName);
				map.put("svCurrent",svCurrent);
			}
		}finally {
			databaseHelper.close(conn, statement, result);
		}
		return map;
	}
	public TestingMessage queryTestingRecord(String sql, Object... params) throws SQLException {
		TestingMessage message = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, sql, params);
			result = statement.executeQuery();
			if (result != null) {
				message = new TestingMessage();
				if (result.next()) {// 根据remark+sequenceId只能取到的值是唯一的。
					message = new TestingMessage();
					message.setRemark(result.getString(1));
					message.setSfc(result.getString(2));
					message.setResourceId(result.getString(3));
					message.setChannelId(result.getInt(4));
					message.setSequenceId(result.getInt(5));
					message.setCycle(result.getInt(6));
					message.setStepId(result.getInt(7));
					message.setStepName(result.getString(8));
					message.setTestTimeDuration(result.getBigDecimal(9));
					message.setTimestamp(result.getTimestamp(10));
					message.setSvIcRange(result.getBigDecimal(11));
					message.setSvIvRange(result.getBigDecimal(12));
					message.setPvVoltage(result.getBigDecimal(13));
					message.setPvCurrent(result.getBigDecimal(14));
					message.setPvIr(result.getBigDecimal(15));
					message.setPvTemperature(result.getBigDecimal(16));
					message.setPvChargeCapacity(result.getBigDecimal(17));
					message.setPvDischargeCapacity(result.getBigDecimal(18));
					message.setPvChargeEnergy(result.getBigDecimal(19));
					message.setPvDischargeEnergy(result.getBigDecimal(20));
					message.setBusinessCycle(result.getInt(21));
				}
			}
		} finally {
			databaseHelper.close(conn, statement, result);
		}
		return message;
	}

	public Integer queryCycleNumber(String remark) throws SQLException {
		Integer cycleNum = 0;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, QUERY_CYCLE_NUMBER_SQL, remark, remark);
			result = statement.executeQuery();
			if (result != null) {
				while (result.next()) {
					cycleNum = result.getInt(1);
				}
			}
		} finally {
			databaseHelper.close(conn, statement, result);
		}
		return cycleNum;
	}

	public Map<Integer, MDStepInfo> queryMasterDataStepRecord(String remark) throws SQLException {
		Map<Integer, MDStepInfo> data = Maps.newHashMap();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, stepsql, remark);
			rs = statement.executeQuery();
			while (rs.next()) {
				MDStepInfo mdStepInfo = new MDStepInfo();
				mdStepInfo.setSite(rs.getString(2));
				mdStepInfo.setRemark(rs.getString(3));
				mdStepInfo.setStepId(rs.getInt(4));
				mdStepInfo.setStepName(rs.getString(5));
				mdStepInfo.setScriptCurrent(rs.getString(6));
				mdStepInfo.setScriptVoltage(rs.getString(7));
				mdStepInfo.setScriptTemperature(rs.getString(8));
				mdStepInfo.setScriptTime(rs.getString(9));
				mdStepInfo.setScriptCapacity(rs.getString(10));
				mdStepInfo.setScriptEnergy(rs.getString(11));
				mdStepInfo.setCycleSignalStep(rs.getBoolean(12));
				mdStepInfo.setDeltaVoltage(rs.getBigDecimal(13));
				mdStepInfo.setSvPower(rs.getBigDecimal(14));
				mdStepInfo.setSvIr(rs.getBigDecimal(15));
				mdStepInfo.setSvVoltage(rs.getBigDecimal(16));
				mdStepInfo.setSvCapacity(rs.getBigDecimal(17));
				mdStepInfo.setSvCurrent(rs.getBigDecimal(18));
				mdStepInfo.setSvStepEndCapacity(rs.getBigDecimal(19));
				mdStepInfo.setSvStepEndCurrent(rs.getBigDecimal(20));
				mdStepInfo.setSvStepEndVoltage(rs.getBigDecimal(21));
				mdStepInfo.setSvStepEndTemperature(rs.getBigDecimal(22));
				mdStepInfo.setSvEnergy(rs.getBigDecimal(23));
				mdStepInfo.setSvTemperature(rs.getBigDecimal(24));
				mdStepInfo.setSvTime(rs.getBigDecimal(25));
				mdStepInfo.setCycleCount(rs.getInt(26));
				mdStepInfo.setConditionType(rs.getString(27));
				mdStepInfo.setConditionOperationalCharacter(rs.getString(28));
				mdStepInfo.setConditionValue(rs.getBigDecimal(29));
				mdStepInfo.setGotoStep(rs.getString(30));
				mdStepInfo.setStartStep(rs.getString(31));
				mdStepInfo.setStartSoc(rs.getBigDecimal(32));
				mdStepInfo.setSocIncrement(rs.getBigDecimal(33));
				mdStepInfo.setEndSoc(rs.getBigDecimal(34));
				data.put(mdStepInfo.getStepId(), mdStepInfo);
			}

		} finally {
			databaseHelper.close(conn, statement, rs);
		}
		return data;
	}

	public String queryMaxBusinessCycle(String remark) throws SQLException {
		String maxBusinessCycle = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, getBusinessCycle, remark);
			rs = statement.executeQuery();
			if (rs.next()){
				maxBusinessCycle = rs.getString(1);
			}
		}finally {
			databaseHelper.close(conn, statement, rs);
		}
		return maxBusinessCycle;
	}


	/**
	 *
	 * @param remark
	 * @return
	 * @throws SQLException
     */
	public String queryMaxLogicNumber(String remark) throws SQLException {
		String maxLogicNum = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, getStepLogicSql, remark);
			rs = statement.executeQuery();
			if (rs.next()){
				maxLogicNum = rs.getString(1);
			}
		}finally {
			databaseHelper.close(conn, statement, rs);
		}
		return maxLogicNum;
	}

	/**
	 *
	 * @param remark
	 * @return
	 * @throws SQLException
     */
	public MasterData queryMasterDataRecord(String remark) throws SQLException {
		MasterData masterData = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, mastersql, remark);
			rs = statement.executeQuery();
			if (rs.next()) {
				masterData = new MasterData();
				masterData.setHandle(rs.getString(1));
				masterData.setSite(rs.getString(2));
				masterData.setRemark(rs.getString(3));
				masterData.setProcessId(rs.getString(4));
				masterData.setTestRequestId(rs.getString(5));
				masterData.setGroupName(rs.getString(6));
				masterData.setSfc(rs.getString(7));
				masterData.setEngineer(rs.getString(8));
				masterData.setPackageId(rs.getString(9));
				masterData.setProject(rs.getString(10));
				masterData.setDescription(rs.getString(11));
				masterData.setSvModel(rs.getString(12));
				masterData.setSvCapacityValue(rs.getBigDecimal(13));
				masterData.setFixtureType(rs.getString(14));
				masterData.setSvInitFixtureForce(rs.getBigDecimal(15));
				masterData.setSvInitWeight(rs.getBigDecimal(16));
				masterData.setSvInitVolume(rs.getBigDecimal(17));
				masterData.setSvInitIr(rs.getBigDecimal(18));
				masterData.setSvInitOcv(rs.getBigDecimal(19));
				masterData.setSvInitThickness(rs.getBigDecimal(20));
				masterData.setDaysOrCycle(rs.getDouble(21));
				masterData.setSvLowerU(rs.getBigDecimal(22));
				masterData.setSvUpperU(rs.getBigDecimal(23));
				masterData.setSvTemperature(rs.getBigDecimal(24));
				masterData.setSvTimeDuration(rs.getBigDecimal(25));
				masterData.setSvChargeCurrent(rs.getBigDecimal(26));
				masterData.setSvDischargeCurrent(rs.getBigDecimal(27));
				masterData.setSvChargeVoltage(rs.getBigDecimal(28));
				masterData.setSvDischargeVoltage(rs.getBigDecimal(29));
				masterData.setSvChargePower(rs.getBigDecimal(30));
				masterData.setSvDischargePower(rs.getBigDecimal(31));
				masterData.setCycleNumber(rs.getInt(32));
				masterData.setSoc(rs.getBigDecimal(33));
				masterData.setPluseTimes(rs.getInt(34));
				masterData.setChargeMulti(rs.getString(35));
				masterData.setDischargeMulti(rs.getString(36));
				masterData.setStoreEndCondition(rs.getString(37));
				masterData.setStoreCrossCondition(rs.getString(38));
				masterData.setCycleEndCondition(rs.getString(39));
				masterData.setCycleCrossCondition(rs.getString(40));
				masterData.setSimEndCondition(rs.getString(41));
				masterData.setSimCrossCondition(rs.getString(42));
				masterData.setIsoDischarge(rs.getBoolean(43));
				masterData.setIsoDischargeDuration(rs.getString(44));
				masterData.setConstantIrValue(rs.getString(45));
				masterData.setCycleTemperature(rs.getString(46));
				masterData.setStoreTemperature(rs.getString(47));
				masterData.setTxStatus(rs.getString(48));
				masterData.setCycleNumber(rs.getInt(49));
				masterData.setRootRemark(rs.getString(50));
			}
		} finally {
			databaseHelper.close(conn, statement, rs);
		}
		return masterData;
	}

	public String getStatement(String remark) throws SQLException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = databaseHelper.getConnection();
			statement = databaseHelper.getStatement(conn, listSql, remark, STATUS);
			rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} finally {
			databaseHelper.close(conn, statement, rs);
		}
		return null;
	}

	/**
	 * @param testingData
	 * @return
	 * @throws SQLException
	 */
	public AlertListInfoModel queryAlterInfoNumber(TestingResultData testingData, List<AlertListInfoModel> listInfo) throws SQLException{
		// 判断报警单表是否已经有此测试数据对应的流程号的报警单，且状态为new，否则重新生成对应的流程报警单号
		AlertListInfoModel alertListInfoModel = null;
		String remark = testingData.getRemark();
		String alertListId = UUID.randomUUID().toString();
		String handle = StringUtils.join("TxAlertListInfoBO:", testingData.getSite(), ",", alertListId);
		boolean state = setState(remark, handle);
		alertListInfoModel = new AlertListInfoModel();
		if (state) {
			alertListInfoModel.setRemark(testingData.getRemark());
			alertListInfoModel.setAlertLevel(testingData.getAlertLevel());
			alertListInfoModel.setAlertListId(alertListId);
			alertListInfoModel.setIsContainMainData(testingData.getIsContainMainData());
			alertListInfoModel.setSite(testingData.getSite());
			alertListInfoModel.setAlertListInfohandle(handle);
			alertListInfoModel.setStatus(testingData.getStatus());
			listInfo.add(alertListInfoModel);
		} else {
			String newHandle = getHandleForRedis(remark);
			if (newHandle != null) {
				alertListInfoModel.setAlertListInfohandle(newHandle);
			}else {
				alertListInfoModel.setRemark(testingData.getRemark());
				alertListInfoModel.setAlertLevel(testingData.getAlertLevel());
				alertListInfoModel.setAlertListId(alertListId);
				alertListInfoModel.setIsContainMainData(testingData.getIsContainMainData());
				alertListInfoModel.setSite(testingData.getSite());
				alertListInfoModel.setAlertListInfohandle(handle);
				alertListInfoModel.setStatus(testingData.getStatus());
				listInfo.add(alertListInfoModel);
			}
		}
		return alertListInfoModel;
	}

	/**
	 * @param testingResultData
	 * @throws SQLException
	 */
	public void modifiedStatus(TestingResultData testingResultData, int stateType) throws SQLException {
		String mdWorkType = null;
		// 更新主数据状态，
		switch (stateType) {
		case 1:
			mdWorkType = SystemConfig.WORKTYPEPROCESS;
			break;
		case 3:
			mdWorkType = SystemConfig.WORKTYPEPROTECTED;
			break;
		case 0:
			mdWorkType = SystemConfig.WORKTYPECLOSE;
			break;
		}
		Object[] updateParams = new Object[] { mdWorkType, testingResultData.getTestingMessage().getBusinessCycle(),
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), SystemConfig.MODIFIEDUSER,
				testingResultData.getTestingMessage().getRemark() };
		databaseHelper.insertOrUpdate(updateSql, updateParams);

	}

	/**
	 * @param remark
	 * @param cycleId
	 * @param stepId
	 * @return
	 */
	public String extractData(String remark, int cycleId, int stepId) {
		String state = null;
		databaseHelper.extract(remark, cycleId, stepId);
		return state;
	}

	/**
	 * @param ListInfos
	 * @throws SQLException
	 */
	public void insertAlertListInfo(List<AlertListInfoModel> ListInfos) throws SQLException {
		List<Object[]> records = new ArrayList<>();
		for (AlertListInfoModel alertListInfoModel : ListInfos) {
			Object[] str = new Object[] { alertListInfoModel.getAlertListInfohandle(), alertListInfoModel.getSite(),
					alertListInfoModel.getAlertListId(), alertListInfoModel.getRemark(), alertListInfoModel.getStatus(),
					"", "", "", "", "", "", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER };
			records.add(str);
		}
		databaseHelper.batchInsertOrUpdate(alertListsql, records ,"insertAlertListInfo");
	}

	/**
	 * @param alertInfos
	 * @throws SQLException
	 */
	public void insertAlertInfo(List<AlertInfoModel> alertInfos) throws SQLException {
		List<Object[]> records = new ArrayList<>();
		for (AlertInfoModel alertInfoModel : alertInfos) {
			Object[] str = new Object[] { alertInfoModel.getAlertInfoHandle(), alertInfoModel.getSite(),
					alertInfoModel.getRemark(), alertInfoModel.getSfc(), alertInfoModel.getCategory(),
					alertInfoModel.getSequenceNumber(), alertInfoModel.getAlertListInfohandle(),
					alertInfoModel.getStatus(), alertInfoModel.getProcessDataBO(), alertInfoModel.getTimestamp(),
					alertInfoModel.getErpResourceBO(), alertInfoModel.getChannelId(), alertInfoModel.getAlertLevel(),
					alertInfoModel.getDescription(), alertInfoModel.getUpLimit(), alertInfoModel.getLowLimit(),
					alertInfoModel.getOriginalProcessDataBO(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), SystemConfig.CREATEUSER,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), SystemConfig.CREATEUSER };
			records.add(str);
		}
		databaseHelper.batchInsertOrUpdate(alertInfosql, records,"insertAlertInfo");
	}

	/**
	 * @param SubInfos
	 * @throws SQLException
	 */
	public void insertSubChannel(List<SubChannelModel> SubInfos) throws SQLException {
		List<Object[]> records = new ArrayList<>();
		for (SubChannelModel subChannelModel : SubInfos) {
			Object[] str = new Object[] { subChannelModel.getSubHandle(), subChannelModel.getProcehandleBo(),
					subChannelModel.getSubChannelId(), subChannelModel.getSite(), subChannelModel.getRemark(),
					subChannelModel.getSfc(), subChannelModel.getResourceId(), subChannelModel.getChannelId(),
					subChannelModel.getSequenceId(), subChannelModel.getCycleId(), subChannelModel.getStepId(),
					subChannelModel.getTestTimeDuration(), subChannelModel.getVoltage(), subChannelModel.getCurrent(),
					subChannelModel.getIr(), subChannelModel.getTemperature(), subChannelModel.getChargeCapacity(),
					subChannelModel.getDischargeCapacity(), subChannelModel.getChargeEnergy(),
					subChannelModel.getDischargeEnergy(), subChannelModel.getTimestamp(), subChannelModel.getDataflag(),
					subChannelModel.getWorkType(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER };
			records.add(str);
		}
		databaseHelper.batchInsertOrUpdate(subsql, records,"insertSubChannel");
	}

	/**
	 *
	 * @param OrigInfos
	 * @throws SQLException
	 */
	public void insertOriginal(List<OriginalDataModel> OrigInfos) throws SQLException {
		List<Object[]> records = new ArrayList<>();
		for (OriginalDataModel originalDataModel : OrigInfos) {
			Object[] str = new Object[] { originalDataModel.getProcehandle(), originalDataModel.getSite(),
					originalDataModel.getRemark(), originalDataModel.getSfc(), originalDataModel.getResourceId(),
					originalDataModel.getChannelId(), originalDataModel.getSequenceId(), originalDataModel.getCycle(),
					originalDataModel.getStepId(), originalDataModel.getStepName(),
					originalDataModel.getTestTimeDuration(), originalDataModel.getTimestamp(),
					originalDataModel.getSvIcRange(), originalDataModel.getSvIvRange(),
					originalDataModel.getPvVoltage(), originalDataModel.getPvCurrent(), originalDataModel.getPvIr(),
					originalDataModel.getPvTemperature(), originalDataModel.getPvChargeCapacity(),
					originalDataModel.getPvDischargeCapacity(), originalDataModel.getPvChargeEnergy(),
					originalDataModel.getPvDischargeEnergy(), originalDataModel.getSubChannelIdBO1(),
					originalDataModel.getSubChannelIdBO2(), originalDataModel.getSubChannelIdBO3(),
					originalDataModel.getSubChannelIdBO4(), originalDataModel.getSubChannelIdBO5(),
					originalDataModel.getSubChannelIdBO6(), originalDataModel.getDataflag(),
					originalDataModel.getWorkType(), originalDataModel.getIsDataAlert(),
					originalDataModel.getIsCurrAlert(), originalDataModel.getIsVoltlert(),
					originalDataModel.getIsTempAlert(), originalDataModel.getIsCapalert(),
					originalDataModel.getIsTimeAlert(), "", "", originalDataModel.getRootRemark(),
					originalDataModel.getBusinessId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
					SystemConfig.CREATEUSER, originalDataModel.getStepLogicNumber() };
			records.add(str);
		}
		databaseHelper.batchInsertOrUpdate(procesql, records,"insertOriginal");
	}

	public void insertZipData(List<ZipDataModel> zipDataModels) throws SQLException {
		List<Object[]> records = new ArrayList<>();
		for (ZipDataModel zipDataModel : zipDataModels) {
			Object[] str = new Object[] { zipDataModel.getZipHandle(), zipDataModel.getSite(), zipDataModel.getRemark(),
					zipDataModel.getBusinessId(), zipDataModel.getStepId(), 0, 0, 0, 0, 0,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), SystemConfig.CREATEUSER,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), SystemConfig.CREATEUSER };
			records.add(str);
		}
		databaseHelper.batchInsertOrUpdate(zipsql, records,"insertZipData");
	}

}