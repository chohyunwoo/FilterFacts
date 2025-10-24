package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.LoginViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText userIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView statusTextView;
    private Button goToRegisterButton;
    private LoginViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIdEditText = findViewById(R.id.userId_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        statusTextView = findViewById(R.id.status_text_view);
        goToRegisterButton = findViewById(R.id.go_to_register_button);
        vm = new ViewModelProvider(this).get(LoginViewModel.class);

        loginButton.setOnClickListener(view -> {
            String userId = userIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (userId.isEmpty() || password.isEmpty()) {
                statusTextView.setText("아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }
            vm.login(userId, password);
        });

        goToRegisterButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        vm.getLoading().observe(this, loading -> {
            boolean l = loading != null && loading;
            loginButton.setEnabled(!l);
            userIdEditText.setEnabled(!l);
            passwordEditText.setEnabled(!l);
            if (l) statusTextView.setText("로그인 중...");
        });
        vm.getError().observe(this, err -> {
            if (err != null && !err.isEmpty()) {
                statusTextView.setText(err);
            }
        });
        vm.getSuccess().observe(this, ok -> {
            if (ok != null && ok) {
                statusTextView.setText("로그인 성공! 환영합니다.");
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}