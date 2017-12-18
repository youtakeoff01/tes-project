package com.edcs.tds.storm.model;

public class SystemConfig {

	public static final String SUBCHANNEL_NAME = "pvSubChannelData";// 子通道的名称前缀
	/* 工步名称开始 */
	public static final String HOLD = "搁置";
	public static final String DISCHARGE = "放电";
	public static final String CHARGE = "充电";
	// public static final String CONSTANT_VOLTAGE_CHARGE = "恒压充电";
	public static final String CONSTANT_CURRENT_VOLTAGE_CHARGE = "恒流恒压充电";
	// public static final String CONSTANT_POWER_CHARGE = "恒功率充电";
	// public static final String CONSTANT_POWER_DISCHARGE = "恒功率放电";
	// public static final String CONSTANT_RESISTANCE_DISCHARGE = "恒阻放电";
	// public static final String SIMULATION_WORKSTEP_CURRENT = "模拟工步（电流模式）";
	// public static final String SIMULATION_WORKSTEP_POWER = "模拟工步（功率模式）";
	/* 工步名称结束 */
	public static final String SITE = "2001";
	/* 邮件通知配置-开始 */
	public static final String MY_EMAIL_ACCOUNT = "TDS-Admin@catlbattery.com";
	public static final String MY_EMAIL_PASSWORD = "Aa123456";
	public static final String MY_EMAIL_SMTPHOST = "Mail.catlbattery.com";
	public static final boolean IS_BOOEAN_SSL = false;
	/* 邮件通知配置-结束 */
	// public static final String URL =
	// "http://172.26.66.35:50000/tes-backing/api/v1/integration/storm/md_process_info";

	public static final String CURRENTSCENENAME = "I";
	public static final String VLOTAGESCENENAME = "V";
	public static final String CAPACITYSCENENAME = "C";
	public static final String TIMESCENENAME = "D";
	public static final String TEMPERATURESENENAME = "T";

	/* 场景参数 */
	public static final String DATASTATE = "dataState";
	public static final String PVCURRENT = "pvCurrent";
	public static final String SVICRANGE = "svIcRange";
	public static final String SVSTEPENDCURRENT = "svStepEndCurrent";
	public static final String SVCURRENT = "svCurrent";
	public static final String SVSTEPENDVOLTAGE = "svStepEndVoltage";
	public static final String SVPOWER = "svPower";
	public static final String UPSTEPNAME = "upStepName";
	public static final String UPSTEPSVCURRENT = "upStepsvCurrent";
	public static final String UPPVCURRENT = "upPvCurrent";
	public static final String UPPVVOLTAGE = "upPvVoltage";
	public static final String CONSTANTIRVALUE = "constantIrValue";
	public static final String PVVOLTAGE = "pvVoltage";
	public static final String SVUPPERU = "svUpperU";
	public static final String SVLOWERU = "svLowerU";
	public static final String PVVOLTAGEFIRST = "pvVoltageFirst";
	public static final String UPSTEPPVVOLTAGE = "upStepPvVoltage";
	public static final String SVVOLTAGE = "svVoltage";
	public static final String PVCAPACITY = "pvCapacity";
	public static final String SVSTEPENDCAPACITY = "svStepEndCapacity";
	// public static final String SVCAPACITY = "svCapacity";
	public static final String SVCAPACITYVALUE = "svCapacityValue";
	public static final String TESTTIMEDURATION = "testTimeDuration";
	public static final String SVTIME = "svTime";
	public static final String TIME_CONTANT_P = "timeContantP";
	public static final String UPTESTTIMEDURATION = "upTesttimeDuration";
	public static final String STEPRELATIVESEQ = "stepRelativeSeq";
	public static final String PROTECTRELATIVESEQ = "continueRelativeSeq";

	/* 同步服务 */
	public static final String WORKTYPEPROCESS = "inprocess";
	public static final String WORKTYPEPROTECTED = "protected";
	public static final String WORKTYPECLOSE = "close";
	public static final String CREATEUSER = "STORM";
	public static final String MODIFIEDUSER = "STORM";
	
	public static final int OVERTIME = 60*60*24*30*2;

}
