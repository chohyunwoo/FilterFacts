package com.example.myapplication;

public class UserJoinRequest {
    public String userId;
    public String password;
    public String email;

    public UserJoinRequest(String userId, String password, String email) {
        this.userId = userId;
        this.password = password;
        this.email = email;
    }
}