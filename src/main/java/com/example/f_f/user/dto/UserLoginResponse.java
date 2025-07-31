package com.example.f_f.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {

    private String message;

    public UserLoginResponse(String message) {
        this.message = message;
    }
}
