package personal.fields;

import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import personal.fields.server.NettyChatServer;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NettyServerBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerBootStrap.class);

    @Autowired
    private NettyChatServer nettyChatServer;

    public static void main(String[] args) {

        SpringApplication.run(NettyServerBootStrap.class);

    }

    @PostConstruct
    public void start() {
        try {

        this.nettyChatServer.start();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
