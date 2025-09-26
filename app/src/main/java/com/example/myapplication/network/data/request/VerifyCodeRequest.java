package com.example.myapplication.network.data.request;

public class VerifyCodeRequest {
    public String email;
    public String purpose;
    public String code;

    public VerifyCodeRequest(String email, String purpose, String code) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
    }
}