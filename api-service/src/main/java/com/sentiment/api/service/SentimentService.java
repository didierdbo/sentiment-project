package com.sentiment.api.service;

import com.sentiment.api.model.Analysis;
import com.sentiment.api.model.AnalysisResponse;
import com.sentiment.api.model.MLResponse;
import com.sentiment.api.repository.AnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SentimentService {

    @Autowired
    private MLServiceClient mlServiceClient;  // Inject separate service

    @Autowired
    private AnalysisRepository repository;

    // Simple test method
    @Cacheable("test-cache")
    public String testCache(String input) {
        log.info("=== NOT FROM CACHE - Computing for: {}", input);
        return "Result for: " + input;
    }

    public AnalysisResponse analyze(String text) {
        log.info("Analyzing text: {}", text.substring(0, Math.min(50, text.length())));

        try {
            // Call external service - cache will work
            MLResponse mlResponse = mlServiceClient.predict(text);

            // Save to DB
            Analysis analysis = new Analysis(
                    text,
                    mlResponse.getSentiment(),
                    mlResponse.getConfidence()
            );
            analysis = repository.save(analysis);

            return new AnalysisResponse(analysis);

        } catch (Exception e) {
            log.error("Error analyzing sentiment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze sentiment: " + e.getMessage());
        }
    }

    public List<AnalysisResponse> getHistory() {
        return repository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(AnalysisResponse::new)
                .collect(Collectors.toList());
    }
}