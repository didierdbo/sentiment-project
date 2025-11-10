package com.sentiment.api.controller;

import com.sentiment.api.model.AnalysisRequest;
import com.sentiment.api.model.AnalysisResponse;
import com.sentiment.api.service.SentimentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Slf4j
public class SentimentController {

    @Autowired
    private SentimentService sentimentService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeSentiment(
            @Valid @RequestBody AnalysisRequest request) {
        log.info("Received analysis request");
        AnalysisResponse response = sentimentService.analyze(request.getText());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResponse>> getHistory() {
        log.info("Fetching analysis history");
        return ResponseEntity.ok(sentimentService.getHistory());
    }
}