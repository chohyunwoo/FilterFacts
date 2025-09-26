package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.myapplication.R;
import com.example.myapplication.common.TokenManager;
import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.data.request.UserLoginRequest;
import com.example.myapplication.network.data.response.UserLoginResponse;
import com.example.myapplication.network.common.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText userIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView statusTextView;
    private Button goToRegisterButton;

    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIdEditText = findViewById(R.id.userId_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        statusTextView = findViewById(R.id.status_text_view);
        goToRegisterButton = findViewById(R.id.go_to_register_button);

        // ApiClient 통해서 Retrofit 생성
        ApiClient apiClient = new ApiClient(getApplicationContext());
        tokenManager = new TokenManager(getApplicationContext());
        apiService = apiClient.getRetrofit().create(ApiService.class);

        loginButton.setOnClickListener(view -> {
            String userId = userIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (userId.isEmpty() || password.isEmpty()) {
                statusTextView.setText("아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            UserLoginRequest requestBody = new UserLoginRequest(userId, password);

            Call<UserLoginResponse> call = apiService.login(requestBody);
            call.enqueue(new Callback<UserLoginResponse>() {
                @Override
                public void onResponse(Call<UserLoginResponse> call, Response<UserLoginResponse> response) {
                    Log.d(TAG, "RESPONSE ← code=" + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        UserLoginResponse tokenResponse = response.body();
                        tokenManager.saveTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
                        statusTextView.setText("로그인 성공! 환영합니다.");

                        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "로그인 실패: code=" + response.code());
                        statusTextView.setText("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
                    }
                }

                @Override
                public void onFailure(Call<UserLoginResponse> call, Throwable t) {
                    Log.e(TAG, "NETWORK FAIL: " + t.getMessage());
                    statusTextView.setText("서버와 통신할 수 없습니다.");
                }
            });
        });

        goToRegisterButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}