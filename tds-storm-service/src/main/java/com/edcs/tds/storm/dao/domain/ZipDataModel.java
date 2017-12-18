package com.edcs.tds.storm.dao.domain;

/**
 * Created by caisl2 on 2017/8/27.
 */
public class ZipDataModel {
    private String zipHandle;
    private String site;
    private String remark;
    private int businessId;
    private int stepId;
    private String createdDate;
    private String createdUser;
    private String modifiedDate;
    private String modifiedUser;

    public ZipDataModel() {
    }

    public String getZipHandle() {
        return zipHandle;
    }

    public void setZipHandle(String zipHandle) {
        this.zipHandle = zipHandle;
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

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
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
