package com.example.myapplication.network.common;


import com.example.myapplication.network.data.request.SendCodeRequest;
import com.example.myapplication.network.data.request.UserJoinRequest;
import com.example.myapplication.network.data.request.UserLoginRequest;
import com.example.myapplication.network.data.response.UserLoginResponse;
import com.example.myapplication.network.data.request.VerifyCodeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<UserLoginResponse> login(@Body UserLoginRequest request);

    // 이메일 인증 코드 전송
    @POST("api/email")
    Call<okhttp3.ResponseBody> sendCode(@Body SendCodeRequest body);

    // 이메일 인증 코드 검증
    @POST("api/email/verify")
    Call<okhttp3.ResponseBody> verifyCode(@Body VerifyCodeRequest body);

    // 회원가입
    @POST("api/auth/register")
    Call<Void> register(@Body UserJoinRequest body);
}