package com.edcs.tds.storm.topology;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Created by CaiSL2 on 2017/10/31.
 */
public class TestRedis {
    private static String url = "jdbc:sap://172.26.119.70:30015/TES";
    private static String username = "TES";
    private static String password = "Aa123456";

    public static void main(String[] args) {
        Connection conn =null;
        Statement st = null;
        ResultSet rs = null;
        String remark = null;
        List<String> lists = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            st = conn.createStatement();
            rs = st.executeQuery("select distinct a.remark from (select remark,sequence_id,ST_BUSINESS_CYCLE,created_date_time,STEP_LOGIC_NUMBER from TX_ORIGINAL_PROCESS_DATA b where  EXISTS (SELECT * FROM MD_PROCESS_INFO c WHERE c.remark=b.remark) and b.created_date_time >'2017-10-31')a  left join (  select remark,sequence_id,ST_BUSINESS_CYCLE,created_date_time,STEP_LOGIC_NUMBER from TX_ORIGINAL_PROCESS_DATA b where  EXISTS (SELECT * FROM MD_PROCESS_INFO c WHERE c.remark=b.remark) and b.created_date_time >'2017-10-31') b on a.remark =b.remark and b.sequence_id-a.sequence_id=1 where a.ST_BUSINESS_CYCLE>b.ST_BUSINESS_CYCLE");
            lists = Lists.newArrayList();
            while (rs.next()) {
                remark = rs.getString(1);
                lists.add(remark);
            }
            int data1 = 0;
            int data2 = 0;
            int data3 = 0;
           /* int data4 = 0;
            int data5 = 0;
            int data6 = 0;*/

            // list = ;
            for (String str : lists) {
                // List<Integer> datas = new ArrayList<>();
                Map<String, Integer> datas = Maps.newHashMap();
                // String str = "T3-20170622-2255-705501_Cycle_3_C501-F94_8981";
                Jedis jedis1 = new Jedis("172.26.38.69", 8000);
                // System.out.println(jedis.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis1.get("TES:businessCycle:" + str))) {
                    data1 = Integer.valueOf(jedis1.get("TES:businessCycle:" + str));
                    datas.put("jedis1", data1);
                }
                Jedis jedis2 = new Jedis("172.26.38.69", 8001);
                // System.out.println(jedis1.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis2.get("TES:businessCycle:" + str))) {
                    data2 = Integer.valueOf(jedis2.get("TES:businessCycle:" + str));
                    datas.put("jedis2", data2);
                }
                Jedis jedis3 = new Jedis("172.26.38.69", 8002);
                // System.out.println(jedis2.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis3.get("TES:businessCycle:" + str))) {
                    data3 = Integer.valueOf(jedis3.get("TES:businessCycle:" + str));
                    datas.put("jedis3", data3);
                }
               /* Jedis jedis4 = new Jedis("172.26.119.68", 8000);
                // System.out.println(jedis3.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis4.get("TES:stepLogicNumber:" + str))) {
                    data4 = Integer.valueOf(jedis4.get("TES:stepLogicNumber:" + str));
                    datas.put("jedis4", data4);
                }
                Jedis jedis5 = new Jedis("172.26.119.68", 8002);
                // System.out.println(jedis4.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis5.get("TES:stepLogicNumber:" + str))) {
                    data5 = Integer.valueOf(jedis5.get("TES:stepLogicNumber:" + str));
                    datas.put("jedis5", data5);
                }
                Jedis jedis6 = new Jedis("172.26.119.68", 8001);
                // System.out.println(jedis5.get("TES:stepLogicNumber:" + str));
                if (StringUtils.isNotEmpty(jedis6.get("TES:stepLogicNumber:" + str))) {
                    data6 = Integer.valueOf(jedis6.get("TES:stepLogicNumber:" + str));
                    datas.put("jedis6", data6);
                }*/

                if (datas.size() > 1) {
                    List<String> keys = Lists.newArrayList();
                    int max = 0;
                    String maxKey = "";
                    for (Map.Entry<String, Integer> entry : datas.entrySet()) {
                        if (entry.getValue() < max) {
                            keys.add(entry.getKey());
                        } else {
                            if(max!=0){
                                keys.add(maxKey);
                            }
                            max = entry.getValue();
                            maxKey = entry.getKey();

                        }
                    }
                    for (String string : keys) {
                        String jedis = string;
                        if (jedis == "jedis1") {
                            jedis1.del("TES:businessCycle:" + str);
                        }
                        if (jedis == "jedis2") {
                            jedis2.del("TES:businessCycle:" + str);
                        }
                        if (jedis == "jedis3") {
                            jedis3.del("TES:businessCycle:" + str);
                        }
                        /*if (jedis == "jedis4") {
                            jedis4.del("TES:stepLogicNumber:" + str);
                        }
                        if (jedis == "jedis5") {
                            jedis5.del("TES:stepLogicNumber:" + str);
                        }
                        if (jedis == "jedis6") {
                            jedis6.del("TES:stepLogicNumber:" + str);
                        }*/
                    }
                    System.out.println("出现问题的remark："+str);
                }else{
                    System.out.println("*********没有出现问题的remark："+str);
                }
                if (jedis1 != null)
                    jedis1.close();
                if (jedis2 != null)
                    jedis2.close();
                if (jedis3 != null)
                    jedis3.close();
               /* if (jedis4 != null)
                    jedis4.close();
                if (jedis5 != null)
                    jedis5.close();
                if (jedis6 != null)
                    jedis6.close();*/
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
