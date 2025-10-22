package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.common.ApiService;
import com.example.myapplication.network.data.request.SendCodeRequest;
import com.example.myapplication.network.data.request.UserJoinRequest;
import com.example.myapplication.network.data.request.VerifyCodeRequest;
import com.google.gson.Gson;
import okhttp3.ResponseBody;
import retrofit2.*;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText emailEditText, codeEditText, userIdEditText, passwordEditText;
    private Button sendCodeButton, resendCodeButton, verifyCodeButton, registerButton;
    private TextView statusTextView, codeErrorTextView;

    private ApiService apiService;
    private Gson gson;

    private boolean emailVerified = false;
    private String lastEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();

        // ApiClient로 Retrofit 가져오기
        ApiClient apiClient = new ApiClient(getApplicationContext());
        apiService = apiClient.getRetrofit().create(ApiService.class);

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
        codeErrorTextView = findViewById(R.id.code_error_text_view);

        registerButton.setEnabled(false); // 인증 전 비활성화
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
            if (password.length() < 6) {
                setStatus("비밀번호는 6자 이상이어야 합니다.");
                return;
            }

            UserJoinRequest body = new UserJoinRequest(userId, password, lastEmail);
            log("회원가입 요청: " + body);

            apiService.register(body).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        setStatus("회원가입 성공!");
                        toast("회원가입 성공");

                        // 로그인 화면으로 이동
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
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
                if (resp.isSuccessful()) {
                    setStatus(resend ? "인증코드가 재전송되었습니다." : "인증코드가 전송되었습니다.");
                    toast("인증코드가 전송되었습니다.");
                } else {
                    setStatus("코드 전송 실패 (code=" + resp.code() + ")");
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                setStatus("네트워크 오류: sendCode " + t.getMessage());
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
            codeErrorTextView.setVisibility(View.VISIBLE);
            return;
        }

        VerifyCodeRequest body = new VerifyCodeRequest(email, "signup", code);
        apiService.verifyCode(body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) {
                    emailVerified = true;
                    lastEmail = email;
                    registerButton.setEnabled(true);
                    codeErrorTextView.setVisibility(View.GONE);
                    toast("이메일 인증 성공");
                } else {
                    emailVerified = false;
                    registerButton.setEnabled(false);
                    codeErrorTextView.setVisibility(View.VISIBLE);
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