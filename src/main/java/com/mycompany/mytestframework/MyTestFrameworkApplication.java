package com.mycompany.mytestframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.mycompany.mytestframework",
    "controller",
    "service"
})
@EnableScheduling
@EnableJpaRepositories(basePackages = {
    "com.mycompany.mytestframework",
    "repository"
})
public class MyTestFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTestFrameworkApplication.class, args);
    }
}
