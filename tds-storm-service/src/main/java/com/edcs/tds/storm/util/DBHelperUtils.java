package com.edcs.tds.storm.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHelperUtils {

	static final Logger logger = LoggerFactory.getLogger(DBHelperUtils.class);

	public BasicDataSource dataSource;

	public BasicDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConnection() {
		Connection conn = null;
		try {
			// TODO CHECKME
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("make HANA Connection error:", e);
		}
		return conn;
	}
	public void close(Connection conn, PreparedStatement pst, ResultSet result) {
		try {
			if (result != null)
				result.close();
		} catch (Exception e) {
			logger.error("" + e);
		}

		try {
			if (pst != null)
				pst.close();
		} catch (Exception e) {
			logger.error("" + e);
		}

		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			logger.error("" + e);
		}
	}

	public void close(Connection conn, PreparedStatement pst) {
		try {
			if (pst != null)
				pst.close();
		} catch (Exception e) {
			logger.error("" + e);
		}

		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			logger.error("" + e);
		}
	}
}
