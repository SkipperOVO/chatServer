package personal.fields;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class LogicBootStrap {


    public static void main(String[] args) {
        SpringApplication.run(LogicBootStrap.class);
    }

    @PostConstruct
    public void start() {


    }
}
