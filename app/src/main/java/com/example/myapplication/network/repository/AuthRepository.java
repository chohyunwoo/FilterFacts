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
                    cb.onError("아이디와 비밀번호를 다시 확인해주세요.");
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
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess();
                } else {
                    String errorMessage = "예기치 못한 오류가 발생했습니다. 다시 시도해주세요.";
                    if (response.code() == 400) {
                        errorMessage = "인증 번호를 확인해주세요.";
                    } else if (response.code() == 410) {
                        errorMessage = "인증 시간이 만료되었습니다. 다시 요청해주세요.";
                    }
                    cb.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                cb.onError("네트워크 오류가 발생했습니다. 연결 상태를 확인해주세요.");
            }
        });
    }



    public void register(UserJoinRequest body, SimpleCallback cb) {
        retrofit2.Call<Void> call = apiService.register(body);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess();
                } else {
                    // String errorMessage = "예기치 못한 오류가 발생했습니다. 다시 시도해주세요.";
                    String errorMessage = "이미 등록된 이메일입니다.";
                    if (response.code() == 409) {
                        errorMessage = "이미 사용 중인 아이디입니다.";
                    } else if (response.code() == 412) {
                        errorMessage = "이메일 인증이 필요합니다.";
                    }
                    cb.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                cb.onError("네트워크 오류가 발생했습니다. 연결 상태를 확인해주세요.");
            }
        });
    }
}

