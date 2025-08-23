package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText userIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView statusTextView;
    private Button goToRegisterButton;

    // 에뮬레이터 ↔ PC 로컬 서버
    // http://192.168.13.0:8080
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // 끝의 / 유지
//    private static final String BASE_URL = "http://192.168.13.0:8080/"; // 끝의 / 유지

    private Gson gson;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIdEditText = findViewById(R.id.userId_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        statusTextView = findViewById(R.id.status_text_view);
        goToRegisterButton = findViewById(R.id.go_to_register_button);

        // ----- Retrofit/OkHttp 설정 -----
        gson = new GsonBuilder().setLenient().create();

        // OkHttp 상세 로그 (Logcat 태그: OkHttp)
        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 1. TokenManager 인스턴스 생성
        TokenManager tokenManager = new TokenManager(getApplicationContext());

        // 2. OkHttp 클라이언트 설정
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging) // 로깅만
                .build();

        // 3. Retrofit에 위에서 만든 client 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
        // --------------------------------

        loginButton.setOnClickListener(view -> {
            String userId = userIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (userId.isEmpty() || password.isEmpty()) {
                statusTextView.setText("아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            UserLoginRequest requestBody = new UserLoginRequest(userId, password);

            // Call 생성 → 최종 URL/헤더/바디 로그
            Call<UserLoginResponse> call = apiService.login(requestBody);
            Request raw = call.request();
            Log.d(TAG, "REQUEST → " + raw.method() + " " + raw.url());
            Log.d(TAG, "HEADERS → " + raw.headers());
            Log.d(TAG, "BODY → " + gson.toJson(requestBody));

            call.enqueue(new Callback<UserLoginResponse>() {
                @Override
                public void onResponse(Call<UserLoginResponse> call, Response<UserLoginResponse> response) {
                    Log.d(TAG, "RESPONSE ← code=" + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        // TokenManager를 사용하여 토큰 저장
                        UserLoginResponse tokenResponse = response.body();
                        tokenManager.saveTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
                        Log.d(TAG, "RESPONSE BODY ← " + gson.toJson(response.body()));
                        statusTextView.setText("로그인 성공! 환영합니다.");


                        // ✅ 로그인 성공 시 ChatActivity로 이동
                        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                        // 필요하다면 토큰/아이디 같은 데이터도 전달
                        // intent.putExtra("userId", response.body().getUserId());
                        startActivity(intent);

                        // 뒤로가기 눌렀을 때 다시 로그인화면 안 뜨게 하려면 finish() 추가
                        finish();
                    } else {
                        Log.e(TAG, "로그인 실패: code=" + response.code());
                        statusTextView.setText("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
                    }
                }

                @Override
                public void onFailure(Call<UserLoginResponse> call, Throwable t) {
                    Log.e(TAG, "NETWORK FAIL: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                    statusTextView.setText("서버와 통신할 수 없습니다.");
                }
            });
        });


        // [3] 회원가입 버튼 클릭 리스너 추가
        goToRegisterButton.setOnClickListener(view -> {
            // RegisterActivity로 이동하기 위한 Intent 생성
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent); // 액티비티 시작
        });
    }
}