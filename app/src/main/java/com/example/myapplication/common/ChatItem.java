package com.example.myapplication.common;

public class ChatItem {
    public final String text;
    public final boolean isUser;
    public ChatItem(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }
}