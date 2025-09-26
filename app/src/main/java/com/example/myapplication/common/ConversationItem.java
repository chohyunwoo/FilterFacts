package com.example.myapplication.common;

public class ConversationItem {
    private final long id;
    private final String title;
    private final String createdAt; // 필요 시 사용

    public ConversationItem(long id, String title, String createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCreatedAt() { return createdAt; }
}