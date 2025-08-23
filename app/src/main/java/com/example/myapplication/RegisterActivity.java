package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText emailEditText, codeEditText, userIdEditText, passwordEditText;
    private Button sendCodeButton, resendCodeButton, verifyCodeButton, registerButton;
    private TextView statusTextView;

    private static final String BASE_URL = "http://10.0.2.2:8080/"; // 에뮬레이터 → 로컬호스트

    private Gson gson;
    private ApiService apiService;

    private boolean emailVerified = false;
    private String lastEmail = null; // 인증 성공한 이메일 보관

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        setupRetrofit();
        setupListeners();
    }

    private void bindViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        codeEditText = findViewById(R.id.code_edit_text);
        userIdEditText = findViewById(R.id.userId_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        sendCodeButton = findViewById(R.id.send_code_button);
        resendCodeButton = findViewById(R.id.resend_code_button);
        verifyCodeButton = findViewById(R.id.verify_code_button);
        registerButton = findViewById(R.id.register_button);
        statusTextView = findViewById(R.id.status_text_view);

        registerButton.setEnabled(false); // 인증 전 비활성화
    }

    private void setupRetrofit() {
        gson = new GsonBuilder().setLenient().create();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setupListeners() {
        sendCodeButton.setOnClickListener(v -> sendCode(false));
        resendCodeButton.setOnClickListener(v -> sendCode(true));

        verifyCodeButton.setOnClickListener(v -> verifyCode());

        registerButton.setOnClickListener(v -> {
            if (!emailVerified) {
                toast("이메일 인증을 먼저 완료해주세요.");
                return;
            }
            String userId = text(userIdEditText);
            String password = text(passwordEditText);

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password)) {
                setStatus("아이디/비밀번호를 모두 입력해주세요.");
                return;
            }
            // 서버 @Size(min=6) 기준 맞춤
            if (password.length() < 6) {
                setStatus("비밀번호는 6자 이상이어야 합니다.");
                return;
            }

            System.out.println("lastEmail = " + lastEmail);
            UserJoinRequest body = new UserJoinRequest(userId, password, lastEmail);
            log("회원가입 요청: " + gson.toJson(body));

            apiService.register(body).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        // 201 Created 기대
                        setStatus("회원가입 성공!");
                        toast("회원가입 성공");
                        // finish(); // 필요 시 화면 닫기
                    } else if (resp.code() == 409) {
                        setStatus("이미 존재하는 아이디입니다.");
                    } else {
                        setStatus("회원가입 실패 (code=" + resp.code() + ")");
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    setStatus("서버와 통신할 수 없습니다: " + t.getMessage());
                }
            });
        });
    }

    private void sendCode(boolean resend) {
        String email = text(emailEditText);
        if (TextUtils.isEmpty(email)) {
            setStatus("이메일을 입력해주세요.");
            return;
        }
        SendCodeRequest body = new SendCodeRequest(email, "signup");
        apiService.sendCode(body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                System.out.println("resp.code() = " + resp.code());
                if (resp.isSuccessful()) {
                    setStatus(resend ? "인증코드가 재전송되었습니다." : "인증코드가 전송되었습니다.");
                    toast("인증코드 전송");
                } else {
                    setStatus("코드 전송 실패 (code=" + resp.code() + ")");
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                setStatus("네트워크 오류: sendCode" + t.getMessage());
            }
        });
    }

    private void verifyCode() {
        String email = text(emailEditText);
        String code = text(codeEditText);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(code)) {
            setStatus("이메일과 인증코드를 모두 입력해주세요.");
            return;
        }
        if (code.length() != 6) {
            setStatus("인증코드는 6자리 숫자입니다.");
            return;
        }

        VerifyCodeRequest body = new VerifyCodeRequest(email, "signup", code);
        apiService.verifyCode(body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) {
                    emailVerified = true;
                    lastEmail = email;
                    registerButton.setEnabled(true); // ✅ 인증 완료되면 가입 버튼 활성화
                    setStatus("이메일 인증 완료! 이제 가입할 수 있어요.");
                    toast("이메일 인증 성공");
                } else {
                    emailVerified = false;
                    registerButton.setEnabled(false);
                    setStatus("인증 실패 (code=" + resp.code() + ")");
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                setStatus("네트워크 오류: " + t.getMessage());
            }
        });
    }

    private String text(EditText et) { return et.getText().toString().trim(); }
    private void setStatus(String s) { statusTextView.setText(s); log(s); }
    private void log(String s) { Log.d(TAG, s); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}