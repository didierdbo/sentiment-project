package com.sentiment.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLResponse {
    private String text;
    private String sentiment;
    private Double confidence;
}