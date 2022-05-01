package personal.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NettyServerBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerBootStrap.class);

    public static void main(String[] args) {

        SpringApplication.run(NettyServerBootStrap.class);

        logger.info("NettyServer 启动成功！");
    }

    @PostConstruct
    public void start() {

    }

}
