package com.edcs.tds.storm.topology;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import com.edcs.tds.storm.util.DBHelperUtils;
import com.edcs.tds.storm.util.StormBeanFactory;

public class CalcTest {
	
	public static void main(String[] args) {
		StormBeanFactory bean = new StormBeanFactory("tds-calc-topology.xml");
		
		DBHelperUtils db = bean.getBean(DBHelperUtils.class);
		CalcTest test = new CalcTest();
		String state = test.extract("MD_TEST_4032",1,1,db);
		System.out.println(state);
	}
	
	public String extract(String remark, int cycleId, int stepId,DBHelperUtils db) {
		Connection conn = null;
		CallableStatement c = null;
		String state = null;
		try {
			conn = db.getConnection();
			c = conn.prepareCall("{call \"TES\".\"EXTRACTIONDATA\"(?, ?, ?, ?)}");
			c.setString(1, remark);
			c.setInt(2, cycleId);
			c.setInt(3, stepId);
			c.registerOutParameter(4, Types.VARCHAR);
			c.execute();
			state = c.getString(4);
		} catch (SQLException e) {
			
		} finally {
			try {
				c.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return state;
	}
}
