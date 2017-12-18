package com.edcs.tds.storm.topology;

import java.sql.*;

/**
 * Created by CaiSL2 on 2017/10/23.
 */
public class HanaTest {
    public static void main(String[] args) {
        Connection connection =null;
        long t1 = System.currentTimeMillis();

        String sql = "SELECT B.REMARK,B.ST_BUSINESS_CYCLE, B.STEP_ID,row_number() OVER (PARTITION BY B.REMARK,B.ST_BUSINESS_CYCLE,B.STEP_ID ORDER BY B.SEQUENCE_ID) as ROW_NUM , B.SEQUENCE_ID \" +\n" +
                "\"FROM (SELECT REMARK,ST_BUSINESS_CYCLE,STEP_ID FROM TECH_ZIP_STATUS WHERE C_STATUS=0 LIMIT 3000) A, TX_ORIGINAL_PROCESS_DATA B \" +\n" +
                "\"WHERE A.REMARK=B.REMARK AND A.ST_BUSINESS_CYCLE=B.ST_BUSINESS_CYCLE AND A.STEP_ID=B.STEP_ID \" +\n" +
                "\"GROUP BY B.REMARK, B.ST_BUSINESS_CYCLE, B.STEP_ID,B.SEQUENCE_ID\n";
        try {
            connection = DriverManager.getConnection("jdbc:sap://172.26.66.36:30015/TES", "TES", "Aa123456");
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();

        System.out.println("取出完成,耗时:"+(t2-t1));
    }
}
