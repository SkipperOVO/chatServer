package test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;

@SpringBootApplication
@Component
public class testProperty {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(testProperty.class);
    }

    @PostConstruct
    public void f() {
        System.out.println(env.getProperty("zk.server.port"));
    }
}
