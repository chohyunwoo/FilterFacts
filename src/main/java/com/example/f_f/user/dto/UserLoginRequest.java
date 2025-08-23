package com.example.f_f.user.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    public String getUserId() { return userId; }
    public String getPassword() { return password; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
}