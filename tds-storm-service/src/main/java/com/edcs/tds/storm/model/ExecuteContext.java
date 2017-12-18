package com.edcs.tds.storm.model;

import java.util.List;

import com.edcs.tds.storm.util.CommonUtil;
import com.google.common.collect.Lists;

public class ExecuteContext {

	private MasterData masterData = null;

	private TestingMessage message = null;

	private List<TestingResultData> resultDatas = Lists.newArrayList();

	public MasterData getMasterData() {
		return masterData;
	}

	public void setMasterData(MasterData masterData) {
		this.masterData = masterData;
	}

	public TestingMessage getMessage() {
		return message;
	}

	public void setMessage(TestingMessage message) {
		this.message = message;
	}

	public List<TestingResultData> getResultDatas() {
		return resultDatas;
	}

	public void setResultDatas(List<TestingResultData> resultDatas) {
		this.resultDatas = resultDatas;
	}

	public String generateHandle() {
		if (masterData != null) {
			return CommonUtil.commaLink().join("TxOriginalProcessDataBO:" + masterData.getSite(),
					masterData.getRemark(), masterData.getSfc(), message.getResourceId(), message.getChannelId(),
					message.getSequenceId());
		} else {
			return CommonUtil.commaLink().join("TxOriginalProcessDataBO:" + SystemConfig.SITE, message.getRemark(),
					message.getSfc(), message.getResourceId(), message.getChannelId(), message.getSequenceId());
		}
	}

}
