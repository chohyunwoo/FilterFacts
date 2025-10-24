package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.RegisterViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText emailEditText, codeEditText, userIdEditText, passwordEditText;
    private Button sendCodeButton, resendCodeButton, verifyCodeButton, registerButton;
    private TextView statusTextView, codeErrorTextView;

    private RegisterViewModel vm;

    private boolean emailVerified = false;
    private String lastEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();

        // ApiClient로 Retrofit 가져오기
        vm = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupListeners();

        vm.getStatus().observe(this, s -> { if (s != null) setStatus(s); });
        vm.getEmailVerified().observe(this, ok -> {
            boolean v = ok != null && ok;
            emailVerified = v;
            if (v) {
                lastEmail = text(emailEditText);
                registerButton.setEnabled(true);
                codeErrorTextView.setVisibility(View.GONE);
                toast("이메일 인증 성공");
            } else {
                registerButton.setEnabled(false);
                codeErrorTextView.setVisibility(View.VISIBLE);
            }
        });
        vm.getRegistered().observe(this, ok -> {
            if (ok != null && ok) {
                setStatus("회원가입 성공!");
                toast("회원가입 성공");
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        vm.getError().observe(this, err -> {
            if (err != null && !err.isEmpty()) {
                setStatus(err);
            }
        });
        vm.getLoading().observe(this, loading -> {
            boolean l = loading != null && loading;
            emailEditText.setEnabled(!l);
            codeEditText.setEnabled(!l);
            userIdEditText.setEnabled(!l);
            passwordEditText.setEnabled(!l);
            sendCodeButton.setEnabled(!l);
            resendCodeButton.setEnabled(!l);
            verifyCodeButton.setEnabled(!l);
            registerButton.setEnabled(!l && emailVerified);
        });
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
            vm.register(userId, password, lastEmail);
        });
    }

    private void sendCode(boolean resend) {
        String email = text(emailEditText);
        if (TextUtils.isEmpty(email)) {
            setStatus("이메일을 입력해주세요.");
            return;
        }
        vm.sendCode(email, resend);
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
        vm.verifyCode(email, code);
    }

    private String text(EditText et) { return et.getText().toString().trim(); }
    private void setStatus(String s) { statusTextView.setText(s); log(s); }
    private void log(String s) { Log.d(TAG, s); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}