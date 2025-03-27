package org.knock.knock_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class KnockApplication {

    public static void main(String[] args) {

        System.out.println("Knock Application");
        SpringApplication.run(KnockApplication.class, args);

    }
}
