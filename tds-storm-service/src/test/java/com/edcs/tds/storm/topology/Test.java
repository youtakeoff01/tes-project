package com.edcs.tds.storm.topology;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by caisl2 on 2017/9/7.
 */
public class Test {
    private static String url = "jdbc:sap://172.26.119.70:30015/TES";
    private static String username = "TES";
    private static String password = "Aa123456";

    Jedis jedis = new Jedis("172.26.38.69", 18000);

    /**
     * 返回集合
     *
     * @return
     */
    public List<String> getData(String key) {
        List<String> list = new ArrayList<>();
        list = jedis.lrange(key, 0, 200);

        return list;
    }

    /**
     * 数据写入redis
     *
     * @param key
     * @param i
     */
    public void writeData(String key, int i) {
        jedis.lpush(key, String.valueOf(i));
        if (jedis.llen("key") >= 200) {
            jedis.rpop(key);
        }
    }
}