package com.example.myapplication.network.common;

public class ApiConstants {
    public static final String BASE_URL = "http://34.64.152.129:8080/"; // 끝에 / 유지!

    // Auth
    public static final String AUTH_BASE = BASE_URL + "api/auth";
    public static final String LOGIN = AUTH_BASE + "/login";
    public static final String REGISTER = AUTH_BASE + "/register";
    public static final String REFRESH = AUTH_BASE + "/refresh";
    public static final String LOGOUT = AUTH_BASE + "/logout";

    // Chat
    public static final String CHAT_BASE = BASE_URL + "api/chat";
    public static final String ASK = CHAT_BASE + "/ask";
    public static final String CONVERSATIONS = CHAT_BASE + "/conversations";
    public static final String MESSAGES = CHAT_BASE + "/messages";
}
