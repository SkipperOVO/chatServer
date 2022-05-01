package personal.fields.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class RouterBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RouterBootstrap.class);

    public static void main(String[] args) {
        RouterBootstrap routerBootstrap = new RouterBootstrap();
        routerBootstrap.start();
    }


    public static void start() {

        SpringApplication.run(RouterBootstrap.class);

        logger.info("Router 启动成功。");

    }
}
