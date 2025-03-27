package org.knock.knock_back.dto.dto.crawling;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api.crawling")
public class CrawlingProperties {
    private Map<String, CrawlingConfig> sources;
}
