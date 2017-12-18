package com.edcs.tds.storm.dao.domain;


import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class AlertInfoModel {
    private String AlertInfoHandle;
    private String site;
    private String remark;
    private String sfc;
    private String category;
    private int sequenceNumber;
    private String alertListInfohandle;
    private String status;
    private String processDataBO;
    private Timestamp timestamp;
    private String erpResourceBO;
    private int channelId;
    private int alertLevel;
    private String description;
    private BigDecimal upLimit;
    private BigDecimal lowLimit;
    private String originalProcessDataBO;
    private String createdDate;
    private String createdUser;
    private String modifiedDate;
    private String modifiedUser;

    public AlertInfoModel() {
    }

    public String getAlertInfoHandle() {
        return AlertInfoHandle;
    }

    public void setAlertInfoHandle(String alertInfoHandle) {
        AlertInfoHandle = alertInfoHandle;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getAlertListInfohandle() {
        return alertListInfohandle;
    }

    public void setAlertListInfohandle(String alertListInfohandle) {
        this.alertListInfohandle = alertListInfohandle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessDataBO() {
        return processDataBO;
    }

    public void setProcessDataBO(String processDataBO) {
        this.processDataBO = processDataBO;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getErpResourceBO() {
        return erpResourceBO;
    }

    public void setErpResourceBO(String erpResourceBO) {
        this.erpResourceBO = erpResourceBO;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(int alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUpLimit() {
        return upLimit;
    }

    public void setUpLimit(BigDecimal upLimit) {
        this.upLimit = upLimit;
    }

    public BigDecimal getLowLimit() {
        return lowLimit;
    }

    public void setLowLimit(BigDecimal lowLimit) {
        this.lowLimit = lowLimit;
    }

    public String getOriginalProcessDataBO() {
        return originalProcessDataBO;
    }

    public void setOriginalProcessDataBO(String originalProcessDataBO) {
        this.originalProcessDataBO = originalProcessDataBO;
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
