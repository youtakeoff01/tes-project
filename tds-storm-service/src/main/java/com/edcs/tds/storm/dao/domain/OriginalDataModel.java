package com.edcs.tds.storm.dao.domain;

import java.math.BigDecimal;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class OriginalDataModel {
    private String procehandle;
    private String site;
    private String remark;
    private String sfc;
    private String resourceId;
    private int channelId;//通道号
    private int sequenceId;//记录序号(每一条数据的序号，一个流程这个记录序号一直在累加)
    private int cycle;//循环序号（一个流程中工步与工步之间的循环序号。这个序号是有可能出现问题的，需要处理）(后期可能不用这个字段，因为strom系统上线之后可能机器已经在运行了。可能使用  businessCycle)
    private int stepId;//工步序号
    private int stepLogicNumber;//工步的逻辑序号
    private String stepName;//工步名称
    private BigDecimal testTimeDuration;//测试相对时长
    private String timestamp;
    private BigDecimal svIcRange;//电流量程：正在进行测试的通道的电流量程。用以作为判异阈值计算输入（通道最大量程）
    private BigDecimal svIvRange;//电压量程：正在进行测试的通道的电压量程。用以作为判异阈值计算输入
    private BigDecimal pvVoltage;//电压
    private BigDecimal pvCurrent;//电流
    private BigDecimal pvIr;//内阻
    private BigDecimal pvTemperature;//温度  temperature
    private BigDecimal pvChargeCapacity;//充电容量  充电容量和放电容量一定有一个为0   ccap
    private BigDecimal pvDischargeCapacity;//放电容量 dccap
    private BigDecimal pvChargeEnergy;//充电能量
    private BigDecimal pvDischargeEnergy;//放电能量 dceng
    private String subChannelIdBO1;
    private String subChannelIdBO2;
    private String subChannelIdBO3;
    private String subChannelIdBO4;
    private String subChannelIdBO5;
    private String subChannelIdBO6;
    private int dataflag;
    private int workType;
    private String isDataAlert;
    private String isCurrAlert;
    private String isVoltlert;
    private String isTempAlert;
    private String isCapalert;
    private String isTimeAlert;
    private String rootRemark;
    private int businessId;
    private String createdDate;
    private String createdUser;
    private String modifiedDate;
    private String modifiedUser;

    public OriginalDataModel() {
    }

    public String getProcehandle() {
        return procehandle;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setProcehandle(String procehandle) {
        this.procehandle = procehandle;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSfc() {
        return sfc;
    }

    public void setSfc(String sfc) {
        this.sfc = sfc;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getStepLogicNumber() {
        return stepLogicNumber;
    }

    public void setStepLogicNumber(int stepLogicNumber) {
        this.stepLogicNumber = stepLogicNumber;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public BigDecimal getTestTimeDuration() {
        return testTimeDuration;
    }

    public void setTestTimeDuration(BigDecimal testTimeDuration) {
        this.testTimeDuration = testTimeDuration;
    }

    public BigDecimal getSvIcRange() {
        return svIcRange;
    }

    public void setSvIcRange(BigDecimal svIcRange) {
        this.svIcRange = svIcRange;
    }

    public BigDecimal getSvIvRange() {
        return svIvRange;
    }

    public void setSvIvRange(BigDecimal svIvRange) {
        this.svIvRange = svIvRange;
    }

    public BigDecimal getPvVoltage() {
        return pvVoltage;
    }

    public void setPvVoltage(BigDecimal pvVoltage) {
        this.pvVoltage = pvVoltage;
    }

    public BigDecimal getPvCurrent() {
        return pvCurrent;
    }

    public void setPvCurrent(BigDecimal pvCurrent) {
        this.pvCurrent = pvCurrent;
    }

    public BigDecimal getPvIr() {
        return pvIr;
    }

    public void setPvIr(BigDecimal pvIr) {
        this.pvIr = pvIr;
    }

    public BigDecimal getPvTemperature() {
        return pvTemperature;
    }

    public void setPvTemperature(BigDecimal pvTemperature) {
        this.pvTemperature = pvTemperature;
    }

    public BigDecimal getPvChargeCapacity() {
        return pvChargeCapacity;
    }

    public void setPvChargeCapacity(BigDecimal pvChargeCapacity) {
        this.pvChargeCapacity = pvChargeCapacity;
    }

    public BigDecimal getPvDischargeCapacity() {
        return pvDischargeCapacity;
    }

    public void setPvDischargeCapacity(BigDecimal pvDischargeCapacity) {
        this.pvDischargeCapacity = pvDischargeCapacity;
    }

    public BigDecimal getPvChargeEnergy() {
        return pvChargeEnergy;
    }

    public void setPvChargeEnergy(BigDecimal pvChargeEnergy) {
        this.pvChargeEnergy = pvChargeEnergy;
    }

    public BigDecimal getPvDischargeEnergy() {
        return pvDischargeEnergy;
    }

    public void setPvDischargeEnergy(BigDecimal pvDischargeEnergy) {
        this.pvDischargeEnergy = pvDischargeEnergy;
    }

    public String getSubChannelIdBO1() {
        return subChannelIdBO1;
    }

    public void setSubChannelIdBO1(String subChannelIdBO1) {
        this.subChannelIdBO1 = subChannelIdBO1;
    }

    public String getSubChannelIdBO2() {
        return subChannelIdBO2;
    }

    public void setSubChannelIdBO2(String subChannelIdBO2) {
        this.subChannelIdBO2 = subChannelIdBO2;
    }

    public String getSubChannelIdBO3() {
        return subChannelIdBO3;
    }

    public void setSubChannelIdBO3(String subChannelIdBO3) {
        this.subChannelIdBO3 = subChannelIdBO3;
    }

    public String getSubChannelIdBO4() {
        return subChannelIdBO4;
    }

    public void setSubChannelIdBO4(String subChannelIdBO4) {
        this.subChannelIdBO4 = subChannelIdBO4;
    }

    public String getSubChannelIdBO5() {
        return subChannelIdBO5;
    }

    public void setSubChannelIdBO5(String subChannelIdBO5) {
        this.subChannelIdBO5 = subChannelIdBO5;
    }

    public String getSubChannelIdBO6() {
        return subChannelIdBO6;
    }

    public void setSubChannelIdBO6(String subChannelIdBO6) {
        this.subChannelIdBO6 = subChannelIdBO6;
    }

    public int getDataflag() {
        return dataflag;
    }

    public void setDataflag(int dataflag) {
        this.dataflag = dataflag;
    }

    public int getWorkType() {
        return workType;
    }

    public void setWorkType(int workType) {
        this.workType = workType;
    }

    public String getIsDataAlert() {
        return isDataAlert;
    }

    public void setIsDataAlert(String isDataAlert) {
        this.isDataAlert = isDataAlert;
    }

    public String getIsCurrAlert() {
        return isCurrAlert;
    }

    public void setIsCurrAlert(String isCurrAlert) {
        this.isCurrAlert = isCurrAlert;
    }

    public String getIsVoltlert() {
        return isVoltlert;
    }

    public void setIsVoltlert(String isVoltlert) {
        this.isVoltlert = isVoltlert;
    }

    public String getIsTempAlert() {
        return isTempAlert;
    }

    public void setIsTempAlert(String isTempAlert) {
        this.isTempAlert = isTempAlert;
    }

    public String getIsCapalert() {
        return isCapalert;
    }

    public void setIsCapalert(String isCapalert) {
        this.isCapalert = isCapalert;
    }

    public String getIsTimeAlert() {
        return isTimeAlert;
    }

    public void setIsTimeAlert(String isTimeAlert) {
        this.isTimeAlert = isTimeAlert;
    }

    public String getRootRemark() {
        return rootRemark;
    }

    public void setRootRemark(String rootRemark) {
        this.rootRemark = rootRemark;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }
}
