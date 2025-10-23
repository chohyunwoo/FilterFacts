package com.example.myapplication.network.repository;

import androidx.annotation.NonNull;

import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.common.ApiConstants;
import com.example.myapplication.network.common.ApiService;
import com.example.myapplication.network.data.request.SendCodeRequest;
import com.example.myapplication.network.data.request.VerifyCodeRequest;
import com.example.myapplication.network.data.request.UserJoinRequest;
import com.example.myapplication.network.data.request.UserLoginRequest;
import com.example.myapplication.network.data.response.UserLoginResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthRepository {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final ApiClient apiClient;
    private final ApiService apiService;

    public AuthRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.apiService = apiClient.getRetrofit().create(ApiService.class);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String message);
    }

    public void logout(String refreshToken, LogoutCallback cb) {
        try {
            JSONObject body = new JSONObject();
            body.put("refreshToken", refreshToken == null ? "" : refreshToken);
            RequestBody rb = RequestBody.create(body.toString(), JSON);

            ApiClient.RequestFactory factory = () -> new Request.Builder()
                    .url(ApiConstants.LOGOUT)
                    .post(rb)
                    .header("Content-Type", "application/json")
                    .build();

            apiClient.enqueueWithAuth(factory, new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    cb.onError(e.getMessage());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    response.close();
                    cb.onSuccess();
                }
            });
        } catch (JSONException e) {
            cb.onError(e.getMessage());
        }
    }

    public interface LoginCallback {
        void onSuccess(UserLoginResponse resp);
        void onError(String message);
    }

    public void login(UserLoginRequest request, LoginCallback cb) {
        retrofit2.Call<UserLoginResponse> call = apiService.login(request);
        call.enqueue(new retrofit2.Callback<UserLoginResponse>() {
            @Override public void onResponse(retrofit2.Call<UserLoginResponse> call, retrofit2.Response<UserLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError("code=" + response.code());
                }
            }

            @Override public void onFailure(retrofit2.Call<UserLoginResponse> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public void sendCode(SendCodeRequest body, SimpleCallback cb) {
        retrofit2.Call<ResponseBody> call = apiService.sendCode(body);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) cb.onSuccess(); else cb.onError("code=" + response.code());
            }
            @Override public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) { cb.onError(t.getMessage()); }
        });
    }

    public void verifyCode(VerifyCodeRequest body, SimpleCallback cb) {
        retrofit2.Call<ResponseBody> call = apiService.verifyCode(body);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) cb.onSuccess(); else cb.onError("code=" + response.code());
            }
            @Override public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) { cb.onError(t.getMessage()); }
        });
    }

    public void register(UserJoinRequest body, SimpleCallback cb) {
        retrofit2.Call<Void> call = apiService.register(body);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) cb.onSuccess(); else cb.onError("code=" + response.code());
            }
            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) { cb.onError(t.getMessage()); }
        });
    }
}
