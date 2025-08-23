package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class UserLoginRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;


    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}