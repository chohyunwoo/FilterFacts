package com.example.f_f.user.dto;

import java.util.List;

public class AnswerResponse {
    private String answer;
    private List<String> keywords;
    private String model;
    private long tookMs;

    public AnswerResponse(String answer, List<String> keywords, String model, long tookMs) {
        this.answer = answer; this.keywords = keywords; this.model = model; this.tookMs = tookMs;
    }
    public String getAnswer() { return answer; }
    public List<String> getKeywords() { return keywords; }
    public String getModel() { return model; }
    public long getTookMs() { return tookMs; }
}
