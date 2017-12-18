package com.edcs.tds.storm.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.util.JsonUtils;

/**
 * JDBC Helper
 */
public class DatabaseHelper {

	static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

	private BasicDataSource dataSource = null;

	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConnection() {
		Connection conn = null;
		logger.debug("database pool state: active:{}, idle:{}.", dataSource.getNumActive(), dataSource.getNumIdle());
		try {
			if (dataSource != null && !dataSource.isClosed()) {
				conn = dataSource.getConnection();
				conn.setAutoCommit(false);
			}
		} catch (Exception e) {
			logger.error("get database connection error:", e);
		}
		return conn;
	}

	public void close(Connection conn, PreparedStatement statement, ResultSet result) {
		try {
			if (result != null && !result.isClosed()) {
				result.close();
				result = null;
			}
		} catch (Exception e) {
			logger.error("close JBDC result set error:", e);
		}
		try {
			if (statement != null && !statement.isClosed()) {
				statement.close();
				statement = null;
			}
		} catch (Exception e) {
			logger.error("close JDBC preparedstatement error:", e);
		}
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			logger.error("close JDBC connection error:", e);
		}
	}

	public void close(ResultSet result) {
		close(null, null, result);
	}

	public void close(Connection conn, PreparedStatement statement) {
		close(conn, statement, null);
	}

	public boolean insertOrUpdate(String sql, Object... params) throws SQLException {
		Connection conn = null;
		PreparedStatement statement = null;
		boolean result = false;
		try {
			conn = getConnection();
			statement = conn.prepareStatement(sql);
			logger.debug("execute insert or update, sql:{}.", sql);
			for (int i = 0; i < params.length; i++) {
				statement.setObject((i + 1), params[i]);
			}
			result = statement.execute();
			conn.commit();
		} finally {
			close(conn, statement);
		}
		return result;
	}

	public String extract(String remark, int cycleId, int stepId) {
		Connection conn = null;
		CallableStatement c = null;
		String state = null;
		try {
			conn = getConnection();
			c = conn.prepareCall("{call \"TES\".\"EXTRACTIONDATA\"(?, ?, ?, ?)}");
			c.setString(1, remark);
			c.setInt(2, cycleId);
			c.setInt(3, stepId);
			c.registerOutParameter(4, Types.VARCHAR);
			c.execute();
			state = c.getString(4);
			conn.commit();
			logger.info("extract success,remark:{},cycleId:{},stepId:{},state:{}",remark,cycleId,stepId,state);
		} catch (SQLException e) {
			logger.error("extract error:{},remark:{},cycleId:{},stepId:{},state:{}",e,remark,cycleId,stepId,state);
		} finally {
			close(conn, c);
		}
		return state;
	}

	public boolean batchInsertOrUpdate(String sql, List<Object[]> records,String type){
		Connection conn = null;
		PreparedStatement statement = null;
		boolean result = false;
		try {
			conn = getConnection();
			statement = conn.prepareStatement(sql);
			logger.debug("execute batch insert or update, sql:{}.", sql);
			for (Object[] record : records) {
				for (int i = 0; i < record.length; i++) {
					statement.setObject((i + 1), record[i]);
				}
				statement.addBatch();
			}
			result = statement.executeBatch().length == records.size();
			conn.commit();
		}catch(SQLException e){
			close(conn, statement);
			insertOrUpdata(sql,records,type);//批插换成单插
		} finally {
			close(conn, statement);
		}
		return result;
	}
	
	public void insertOrUpdata(String sql,List<Object[]> records,String type){
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(true);
			for (Object[] record : records) {
				statement = conn.prepareStatement(sql);
				for (int i = 0; i < record.length; i++) {
					statement.setObject((i + 1), record[i]);
				}
				try {
					statement.execute();
				} catch (Exception e) {
					logger.error(type+"error,data:{},msg:{}",JsonUtils.toJson(record),e);
				}finally{
					close(null, statement);
				}
			}
		}catch(Exception e){
			logger.error("insertOrUpdata error,msg:{}",e);
		}finally{
			close(conn, statement);
		}
	}

	public PreparedStatement getStatement(Connection conn, String sql, Object... params) throws SQLException {
		PreparedStatement statement = null;
		statement = conn.prepareStatement(sql);
		for (int i = 0; i < params.length; i++) {
			statement.setObject((i + 1), params[i]);
		}
		return statement;
	}
 

}
