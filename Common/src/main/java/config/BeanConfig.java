package config;

import infrastructure.cache.JedisCache;
import infrastructure.cache.JedisPoolUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class BeanConfig {

    @Value("${redis.ip}")
    private String redisAddress;

    @Value("${redis.port}")
    private int port;


    @Bean
    public JedisPool getJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(500);
        poolConfig.setMaxIdle(32);
        poolConfig.setMaxWaitMillis(100 * 1000);
        poolConfig.setTestOnBorrow(true);
        return new JedisPool(poolConfig, redisAddress, port);
    }

//    @Bean
//    public Jedis getJedis() {
//
//        return JedisPoolUtil.getJedisPoolInstance().getResource();
//
//    }
//
//    @Bean
//    public JedisCache getJedisCache() {
//        return new JedisCache();
//    }

}
