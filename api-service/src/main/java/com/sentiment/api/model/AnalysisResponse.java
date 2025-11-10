package com.sentiment.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {

    private Long id;
    private String text;
    private String sentiment;
    private Double confidence;
    private LocalDateTime analyzedAt;

    public AnalysisResponse(Analysis analysis) {
        this.id = analysis.getId();
        this.text = analysis.getText();
        this.sentiment = analysis.getSentiment();
        this.confidence = analysis.getConfidence();
        this.analyzedAt = analysis.getCreatedAt();
    }
}