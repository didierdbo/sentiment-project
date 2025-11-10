package com.sentiment.api.service;

import com.sentiment.api.model.Analysis;
import com.sentiment.api.model.AnalysisResponse;
import com.sentiment.api.model.MLResponse;
import com.sentiment.api.repository.AnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SentimentService {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AnalysisRepository repository;

    public AnalysisResponse analyze(String text) {
        log.info("Analyzing text: {}", text.substring(0, Math.min(50, text.length())));

        try {
            Map<String, String> request = new HashMap<>();
            request.put("text", text);

            String url = mlServiceUrl + "/predict";
            log.info("Calling ML service at: {}", url);

            MLResponse mlResponse = restTemplate.postForObject(
                    url,
                    request,
                    MLResponse.class
            );

            if (mlResponse == null) {
                throw new RuntimeException("ML service returned null response");
            }

            log.info("ML response: {} (confidence: {})",
                    mlResponse.getSentiment(), mlResponse.getConfidence());

            Analysis analysis = new Analysis(
                    text,
                    mlResponse.getSentiment(),
                    mlResponse.getConfidence()
            );
            analysis = repository.save(analysis);

            return new AnalysisResponse(analysis);

        } catch (Exception e) {
            log.error("Error calling ML service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze sentiment: " + e.getMessage());
        }
    }

    public List<AnalysisResponse> getHistory() {
        return repository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(AnalysisResponse::new)
                .collect(Collectors.toList());
    }
}