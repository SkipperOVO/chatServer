package personal.fields.infrastructure.ioc.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Properties;

public class JedisPoolUtil {

    private static JedisPool jedisPool;

    private static Properties properties = new Properties();


    public Jedis getResource() {
        return jedisPool.getResource();
    }


    public static void release(Jedis jedis) {

        jedis.close();

    }



//  单例模式
    private JedisPoolUtil() {
    }


    public static JedisPool getJedisPoolInstance() {
        try {
            // DCL
            if (null == jedisPool) {
                synchronized (JedisPoolUtil.class) {
                    if (null == jedisPool) {
                        JedisPoolConfig poolConfig = new JedisPoolConfig();
//                        poolConfig.setMaxTotal(500);
//                        poolConfig.setMaxIdle(32);
//                        poolConfig.setMaxWaitMillis(100 * 1000);
//                        poolConfig.setTestOnBorrow(true);

                        properties.load(JedisPoolUtil.class.getClassLoader().getResourceAsStream("application.properties"));
                        String redisIp = properties.getProperty("redis.ip");
                        Integer port = Integer.parseInt(properties.getProperty("redis.port"));
                        jedisPool = new JedisPool(poolConfig, redisIp, port, 1500, null);
                    }
                }
            }
            return jedisPool;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }


}