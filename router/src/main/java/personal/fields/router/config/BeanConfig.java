package personal.fields.router.config;

import VO.ServerInfo;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import personal.fields.router.infrastructure.ServerListener;
import personal.fields.router.serverMangerment.BalanceLoader;
import personal.fields.router.serverMangerment.LocalBalanceLoader;
import personal.fields.router.serverMangerment.NettyServerManagerIml;
import personal.fields.router.infrastructure.ServerListenerIml;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import personal.fields.router.serverMangerment.RandomBalanceLoader;

@Configuration
public class BeanConfig {

    private static final Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Autowired
    private Environment env;


    @Bean
    public ZkClient getZkClient() {

        logger.info(env.getProperty("zk.server.ip") + ":" + env.getProperty("zk.server.port"));

        return new ZkClient("192.168.1.101:2181",
                 30000, 30000, new SerializableSerializer());
    }

//    @Bean
//    public BalanceLoader<ServerInfo> lBalanceLoader() {
//        return new LocalBalanceLoader<ServerInfo>();
//    }

    @Bean
    public BalanceLoader<ServerInfo> rBalanceLoader() {
        return new RandomBalanceLoader<ServerInfo>();
    }

}
