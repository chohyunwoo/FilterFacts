package com.example.myapplication.network.common;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.common.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final TokenManager tokenManager;
    private final Retrofit retrofit;

    public ApiClient(Context context) {
        this.tokenManager = new TokenManager(context);

        // Gson
        Gson gson = new GsonBuilder().setLenient().create();

        // OkHttp logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttp client
        this.http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 1. 연결 타임아웃 (15초)
                .readTimeout(60, TimeUnit.SECONDS)    // 2. 응답 타임아웃 (30초)
                .writeTimeout(30, TimeUnit.SECONDS)   // 3. 쓰기 타임아웃 (15초)
                .addInterceptor(logging)
                .build();

        // Retrofit
        this.retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(http)
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    // ====== 기존 enqueueWithAuth 그대로 유지 ======
    @FunctionalInterface
    public interface RequestFactory {
        Request build();
    }

    public void enqueueWithAuth(RequestFactory factory, Callback userCallback) {
        enqueueWithAuthInternal(factory, userCallback, false);
    }

    private void enqueueWithAuthInternal(RequestFactory factory, Callback userCallback, boolean alreadyRetried) {
        Request baseReq = factory.build();
        String auth = tokenManager.getAuthorizationValue();
        Request authed = (auth == null || auth.isEmpty())
                ? baseReq
                : baseReq.newBuilder().header("Authorization", auth).build();

        http.newCall(authed).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                userCallback.onFailure(call, e);
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == 403 && !alreadyRetried) {
                    response.close();
                    attemptRefreshThen(
                            () -> enqueueWithAuthInternal(factory, userCallback, true),
                            () -> {
                                try {
                                    userCallback.onResponse(call, response);
                                } catch (IOException e) {
                                    Log.e("ApiClient", "userCallback.onResponse IOException", e);
                                }
                            }
                    );
                } else {
                    userCallback.onResponse(call, response);
                }
            }
        });
    }

    private void attemptRefreshThen(Runnable onSuccess, Runnable onFail) {
        String refresh = tokenManager.getRefreshToken();
        if (refresh == null || refresh.isEmpty()) {
            onFail.run();
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("refreshToken", refresh);
            RequestBody rb = RequestBody.create(body.toString(), JSON);

            Request refreshReq = new Request.Builder()
                    .url(ApiConstants.REFRESH)
                    .post(rb)
                    .build();

            http.newCall(refreshReq).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onFail.run();
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        onFail.run();
                        return;
                    }
                    String respStr = response.body().string();
                    try {
                        JSONObject json = new JSONObject(respStr);
                        String newAccess = json.optString("accessToken", null);
                        String newRefresh = json.optString("refreshToken", null);
                        if (newAccess == null || newRefresh == null) {
                            onFail.run();
                            return;
                        }
                        tokenManager.setAccessToken(newAccess);
                        tokenManager.setRefreshToken(newRefresh);
                        onSuccess.run();
                    } catch (JSONException e) {
                        onFail.run();
                    }
                }
            });
        } catch (JSONException e) {
            onFail.run();
        }
    }
}