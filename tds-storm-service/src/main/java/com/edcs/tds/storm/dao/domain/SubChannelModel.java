package com.edcs.tds.storm.dao.domain;

import java.math.BigDecimal;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class SubChannelModel {
    private String subHandle;
    private String procehandleBo;
    private int subChannelId;
    private String site;
    private String remark;
    private String sfc;
    private String resourceId;
    private int channelId;
    private int sequenceId;
    private int cycleId;
    private int stepId;
    private BigDecimal testTimeDuration;//测试相对时长
    private BigDecimal voltage;//电压
    private BigDecimal current;//电流
    private BigDecimal ir;//内阻
    private BigDecimal temperature;//温度
    private BigDecimal chargeCapacity;//充电容量  充电容量和放电容量一定有一个为0
    private BigDecimal dischargeCapacity;//放电容量
    private BigDecimal chargeEnergy;//充电能量
    private BigDecimal dischargeEnergy;//放电能量
    private String timestamp;
    private int dataflag;
    private int workType;
    private String createdDate;
    private String createdUser;
    private String modifiedDate;
    private String modifiedUser;

    public SubChannelModel() {
    }

    public String getSubHandle() {
        return subHandle;
    }

    public void setSubHandle(String subHandle) {
        this.subHandle = subHandle;
    }

    public String getProcehandleBo() {
        return procehandleBo;
    }

    public void setProcehandleBo(String procehandleBo) {
        this.procehandleBo = procehandleBo;
    }

    public int getSubChannelId() {
        return subChannelId;
    }

    public void setSubChannelId(int subChannelId) {
        this.subChannelId = subChannelId;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public int getCycleId() {
        return cycleId;
    }

    public void setCycleId(int cycleId) {
        this.cycleId = cycleId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public BigDecimal getTestTimeDuration() {
        return testTimeDuration;
    }

    public void setTestTimeDuration(BigDecimal testTimeDuration) {
        this.testTimeDuration = testTimeDuration;
    }

    public BigDecimal getVoltage() {
        return voltage;
    }

    public void setVoltage(BigDecimal voltage) {
        this.voltage = voltage;
    }

    public BigDecimal getCurrent() {
        return current;
    }

    public void setCurrent(BigDecimal current) {
        this.current = current;
    }

    public BigDecimal getIr() {
        return ir;
    }

    public void setIr(BigDecimal ir) {
        this.ir = ir;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getChargeCapacity() {
        return chargeCapacity;
    }

    public void setChargeCapacity(BigDecimal chargeCapacity) {
        this.chargeCapacity = chargeCapacity;
    }

    public BigDecimal getDischargeCapacity() {
        return dischargeCapacity;
    }

    public void setDischargeCapacity(BigDecimal dischargeCapacity) {
        this.dischargeCapacity = dischargeCapacity;
    }

    public BigDecimal getChargeEnergy() {
        return chargeEnergy;
    }

    public void setChargeEnergy(BigDecimal chargeEnergy) {
        this.chargeEnergy = chargeEnergy;
    }

    public BigDecimal getDischargeEnergy() {
        return dischargeEnergy;
    }

    public void setDischargeEnergy(BigDecimal dischargeEnergy) {
        this.dischargeEnergy = dischargeEnergy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
