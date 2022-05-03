package personal.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import personal.fields.server.NettyChatServer;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NettyServerBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerBootStrap.class);

    public static void main(String[] args) {

        SpringApplication.run(NettyServerBootStrap.class);

    }

    @PostConstruct
    public void start() {
        try {
            new NettyChatServer().start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
