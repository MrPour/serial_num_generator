package org.example;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.*;

public class JedisUtil
{
    private static JedisPool jedisPool = null;
    private static final JedisUtil jedisUtil = new JedisUtil();

    private static ObjectMapper jsonSerializer = new ObjectMapper();
    //单例模式
    private JedisUtil() {
        ResourceBundle properties = ResourceBundle.getBundle("redis");
        String host = properties.getString("redis.host");
        String port = properties.getString("redis.port");
        String password = properties.getString("redis.password");
        String timeout = properties.getString("redis.timeout");
        String maxIdle = properties.getString("redis.pool.maxIdle");
        String maxTotal = properties.getString("redis.pool.maxTotal");
        String maxWaitMillis = properties.getString("redis.pool.maxWaitMillis");
        String testOnBorrow = properties.getString("redis.pool.testOnBorrow");
        JedisPoolConfig config = new JedisPoolConfig();
        //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
        //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
        config.setMaxTotal(Integer.parseInt(maxTotal));
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(Integer.parseInt(maxIdle));
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(Long.parseLong(maxWaitMillis));
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(Boolean.valueOf(testOnBorrow));

        this.jedisPool = new JedisPool(config, host, Integer.parseInt(port), Integer.parseInt(timeout), password);
    }

    /**
     * 单例模式获取JedisUtil实例
     *
     * @return
     */
    public static JedisUtil getInstance() {
        return jedisUtil;
    }
    /**
     * 从jedis连接池中获取获取jedis对象
     *
     * @return
     */
    private Jedis getJedis() {
        return jedisPool.getResource();
    }


    /**
     * 回收jedis(放到finally中)
     *
     * @param jedis
     */
    private void returnJedis(Jedis jedis) {
        if (null != jedis && null != jedisPool) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * 销毁连接(放到catch中)
     *
     * @param jedis
     */
    private static void returnBrokenResource(Jedis jedis) {
        if (null != jedis && null != jedisPool) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * 添加一个键值对，如果键存在不在添加，如果不存在，添加完成以后设置键的有效期
     * @param key
     * @param value
     * @param timeOut 秒
     */
    public void setnx(String key,String value,int timeOut){
        Jedis jedis = getJedis();
        if(0!=jedis.setnx(key, value)){
            jedis.expire(key, timeOut);
        }
        returnJedis(jedis);
    }

    /**
     * 添加一个键值对添加完成以后设置键的有效期
     * @param key
     * @param value
     * @param timeOut 秒
     */
    public void set(String key,String value,int timeOut){
        Jedis jedis = getJedis();
        jedis.set(key,value);
        jedis.expire(key, timeOut);
        returnJedis(jedis);
    }

    /**
     * 获取一个键值对
     * @param key
     * @return
     */
    public <T>T getObjectByKey(String key,Class<T> classType){
        Jedis jedis = getJedis();
        T value = null;
        try {
            if(jedis.exists(key))
            {
                String s = jedis.get(key);
                value = jsonSerializer.readValue(s, classType);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            returnJedis(jedis);
        }
        return  value;
    }

    /**
     * 获取一个键值对
     * @param key
     * @return
     */
    public void setObjectByKey(String key,Object obj,int millisecond){
        Jedis jedis = getJedis();
        try {
            String value = jsonSerializer.writeValueAsString(obj);
            this.set(key,value,millisecond);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 是否存在KEY
     * @param key
     * @return
     */
    public boolean exists(String key) {
        Jedis jedis = getJedis();
        boolean exists = jedis.exists(key);
        returnJedis(jedis);
        return exists;
    }

    /**
     * 重命名KEY
     * @param oldKey
     * @param newKey
     * @return
     */
    public String rename(String oldKey, String newKey) {
        Jedis jedis = getJedis();
        String result = jedis.rename(oldKey, newKey);
        returnJedis(jedis);
        return result;
    }


    public static void main(String[] args) {
        System.out.println(JedisUtil.getInstance().currentTimeSecond());
    }

    /**
     * 获取当前时间
     * @return 秒
     */
    public long currentTimeSecond(){
        Long l = 0L;
        Jedis jedis = getJedis();
        Object obj = jedis.eval("return redis.call('TIME')",0);
        if(obj != null){
            List<String> list = (List)obj;
            l = Long.valueOf(list.get(0));
        }
        returnJedis(jedis);
        return l;
    }

}
