package com.example.f_f.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    @Size(min = 8, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password;

}
