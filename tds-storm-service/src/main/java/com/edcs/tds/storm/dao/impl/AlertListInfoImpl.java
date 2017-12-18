package com.edcs.tds.storm.dao.impl;


import java.sql.SQLException;

import java.util.List;

import com.edcs.tds.storm.db.HanaDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.dao.domain.AlertInfoModel;
import com.edcs.tds.storm.dao.domain.AlertListInfoModel;
import com.edcs.tds.storm.dao.domain.OriginalDataModel;
import com.edcs.tds.storm.dao.domain.SubChannelModel;
import com.edcs.tds.storm.dao.domain.ZipDataModel;


/**
 * Created by caisl2 on 2017/8/27.
 */
public class AlertListInfoImpl {
    private final Logger logger = LoggerFactory.getLogger(AlertListInfoImpl.class);
    private HanaDataHandler hanaDataHandler;
    public void setHanaDataHandler(HanaDataHandler hanaDataHandler) {
        this.hanaDataHandler = hanaDataHandler;
    }

    /**
     * @param listInfos
     */
    public void insertAlertListInfo(List<AlertListInfoModel> listInfos) {
        try {
            hanaDataHandler.insertAlertListInfo(listInfos);
            logger.info("insertAlertListInfo success ,insert number:{}",listInfos.size());
        } catch (SQLException e) {
            logger.error("insertAlertListInfo error,size:{},msg{}",listInfos.size(),e);
        }
    }

    /**
     * @param alertInfos
     */
    public void insertAlertInfo(List<AlertInfoModel> alertInfos) {
        try {
            hanaDataHandler.insertAlertInfo(alertInfos);
            logger.info("insertAlertInfo success ,insert number:{}",alertInfos.size());
        } catch (SQLException e) {
            logger.error("insertAlertInfo error,size:{},msg{}",alertInfos.size(),e);
        }
    }

    /**
     * @param subInfos
     */
    public void insertSubChannel(List<SubChannelModel> subInfos) {
        try {
            hanaDataHandler.insertSubChannel(subInfos);
            logger.info("insertSubChannel success ,insert number:{}",subInfos.size());
        } catch (SQLException e) {
           logger.error("insertSubChannel error,size:{},msg{}" ,subInfos.size(),e);
        }
    }

    /**
     * @param origInfos
     */
    public void insertOriginal(List<OriginalDataModel> origInfos) {
        try {
            hanaDataHandler.insertOriginal(origInfos);
            logger.info("insertOriginal success ,insert number:{}",origInfos.size());
        } catch (SQLException e) {
            logger.error("insertOriginal error,size:{},msg{}" ,origInfos.size(),e);
        }
    }

    /**
     *
     * @param zipDataModels
     */
    public void insertZipData(List<ZipDataModel> zipDataModels) {
        try {
            hanaDataHandler.insertZipData(zipDataModels);
            logger.info("insertZipData success ,insert number:{}",zipDataModels.size());
        } catch (SQLException e) {
            logger.error("insertZipData error,size:{},msg{}" ,zipDataModels.size(),e);
        }
    }

}
