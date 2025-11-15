package com.sentiment.api.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Test configuration for controller tests
 * Provides mock beans needed by the Spring context
 */
@TestConfiguration
public class ControllerTestConfig {

    /**
     * Provides a RestTemplate bean for test context
     * This prevents Spring from trying to auto-configure Redis or other services
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
