package org.knock.knock_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class KnockApplication {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(KnockApplication.class);

        // Heroku 환경 변수 PORT 읽어서 설정
        String port = System.getenv("PORT");
        Map<String, Object> props = new HashMap<>();
        // 로컬 fallback
        props.put("server.port", Objects.requireNonNullElse(port, 8080));

        app.setDefaultProperties(props);
        app.run(args);
    }
}
