package com.edcs.tds.storm.dao.domain;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class AlertListInfoModel {
    private String alertListInfohandle;
    private String site;
    private String alertListId;
    private String status;
    private String remark;
    private String createdDate;
    private String createdUser;
    private String modifiedDate;
    private String modifiedUser;
    private int alertLevel;
    private String isContainMainData;
    public AlertListInfoModel() {
    }

    public int getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(int alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getIsContainMainData() {
        return isContainMainData;
    }

    public void setIsContainMainData(String isContainMainData) {
        this.isContainMainData = isContainMainData;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAlertListInfohandle() {
        return alertListInfohandle;
    }

    public void setAlertListInfohandle(String alertListInfohandle) {
        this.alertListInfohandle = alertListInfohandle;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getAlertListId() {
        return alertListId;
    }

    public void setAlertListId(String alertListId) {
        this.alertListId = alertListId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
