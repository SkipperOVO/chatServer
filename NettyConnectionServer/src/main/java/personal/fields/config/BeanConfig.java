package personal.fields.config;

import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import personal.fields.channelHandler.DispatchMessageHandler;
import personal.fields.channelHandler.NioWebsocketHandler;
import personal.fields.channelMap.SimpleHashMap;
import personal.fields.infrastructure.ioc.cache.JedisCache;
import personal.fields.processor.ACKProcessor;
import personal.fields.processor.C2CChatProcessor;
import personal.fields.processor.HeartBeatProcessor;
import personal.fields.processor.ProcessorContainer;
import redis.clients.jedis.Jedis;

@Configuration
public class BeanConfig {

    @Value("${redis.ip}")
    private String redisAddress;

    @Value("${redis.port}")
    private int port;

    @Bean
    public SimpleHashMap<Integer, Channel>  getChannelIdMap() {
        return new SimpleHashMap<>();
    }

//    @Bean
//    @Scope("prototype")
//    public NioWebsocketHandler getNioWebsocketHandler() {
//        return new NioWebsocketHandler();
//    }

    @Bean
    public DispatchMessageHandler getDispatcher() {
        return new DispatchMessageHandler();
    }

    @Bean
    public JedisCache getJedisCache() {
        return new JedisCache();
    }

    @Bean
    public ProcessorContainer getProcessorContainer() {
        ProcessorContainer container = new ProcessorContainer();
        // Todo 初始化 processor 应该从配置文件读或者 spring 上下文中读，省略
        container.addProcessor("HEARTBEATREQ", new HeartBeatProcessor(container.getThreadPool(), container));
        container.addProcessor("C2CSENDREQ", new C2CChatProcessor(container.getThreadPool(), container));
        container.addProcessor("ACK", new ACKProcessor(container.getThreadPool(), container));

        container.
        return container;
    }

//    @Bean
//    public Jedis getJedis() {
//        return new Jedis(redisAddress, port);
//    }
//
//    @Bean
//    public JedisCache getJedisCache() {
//        return new JedisCache();
//    }

}
