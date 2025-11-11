package com.sentiment.api.service;

import com.sentiment.api.model.MLResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MLServiceClient {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "ml-predictions")
    public MLResponse predict(String text) {
        log.info("=== CACHE MISS - Calling ML service for: {}",
                text.substring(0, Math.min(20, text.length())));

        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        String url = mlServiceUrl + "/predict";
        log.info("Calling ML service at: {}", url);

        MLResponse mlResponse = restTemplate.postForObject(url, request, MLResponse.class);

        if (mlResponse == null) {
            throw new RuntimeException("ML service returned null response");
        }

        log.info("ML response: {} (confidence: {})",
                mlResponse.getSentiment(), mlResponse.getConfidence());

        return mlResponse;
    }
}