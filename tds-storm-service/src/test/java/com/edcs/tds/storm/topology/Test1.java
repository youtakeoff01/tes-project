package com.edcs.tds.storm.topology;

import com.edcs.tds.storm.db.HanaDataHandler;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by caisl2 on 2017/9/11.
 */
public class Test1 {


    public static void main(String[] args) {
       /* Jedis jedis = new Jedis("172.26.66.31",6380);
        String str = "tds-calc-20170907-155836 ACTIVE     34         10           6728024";
        System.out.println(str.contains("INACTIVE"));
        String[] split = str.split("");
        for (String s : split)
        System.out.println(s);*/
     /*   ScanParams sp = new ScanParams();
        sp.match("TES:LOGIC:T3-20170402-0227-613267_Cycle_23_C1325-F95*");
        sp.count(100);
        ScanResult<String> scan = jedis.scan("1", sp);
        System.out.println("scan.getResult().size()"+scan.getResult().size());*/


    }
}
