package config;

import infrastructure.JedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class BeanConfig {

    @Value("${redis.ip}")
    private String redisAddress;

    @Value("${redis.port}")
    private int port;

    @Bean
    public Jedis getJedis() {
        return new Jedis(redisAddress, port);
    }

    @Bean
    public JedisCache getJedisCache() {
        return new JedisCache();
    }

}
