package com.example.myapplication.network.data.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

public class UserLoginResponse {

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresInMs")
    private long expiresInMs;

    @SerializedName("refreshToken")
    private String refreshToken;


    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}