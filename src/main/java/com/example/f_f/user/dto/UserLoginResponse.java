package com.example.f_f.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserLoginResponse {
    private boolean success;
    private String message;

    public UserLoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}