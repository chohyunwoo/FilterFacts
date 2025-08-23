package com.example.myapplication;

public class SendCodeRequest {
    public String email;
    public String purpose;

    public SendCodeRequest(String email, String purpose) {
        this.email = email;
        this.purpose = purpose;
    }
}