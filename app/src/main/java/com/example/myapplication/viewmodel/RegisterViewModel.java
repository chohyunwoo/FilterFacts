package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.data.request.SendCodeRequest;
import com.example.myapplication.network.data.request.UserJoinRequest;
import com.example.myapplication.network.data.request.VerifyCodeRequest;
import com.example.myapplication.network.repository.AuthRepository;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> status = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> emailVerified = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> registered = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        ApiClient apiClient = new ApiClient(application.getApplicationContext());
        this.authRepository = new AuthRepository(apiClient);
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getStatus() { return status; }
    public LiveData<Boolean> getEmailVerified() { return emailVerified; }
    public LiveData<Boolean> getRegistered() { return registered; }
    public LiveData<String> getError() { return error; }

    public void sendCode(String email, boolean resend) {
        if (loading.getValue() != null && loading.getValue()) return;
        loading.postValue(true);
        authRepository.sendCode(new SendCodeRequest(email, "signup"), new AuthRepository.SimpleCallback() {
            @Override public void onSuccess() {
                loading.postValue(false);
                status.postValue(resend ? "인증코드가 재전송되었습니다." : "인증코드가 전송되었습니다.");
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue("코드 전송 실패: " + (message == null ? "알 수 없는 오류" : message));
            }
        });
    }

    public void verifyCode(String email, String code) {
        if (loading.getValue() != null && loading.getValue()) return;
        loading.postValue(true);
        authRepository.verifyCode(new VerifyCodeRequest(email, "signup", code), new AuthRepository.SimpleCallback() {
            @Override public void onSuccess() {
                loading.postValue(false);
                emailVerified.postValue(true);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                emailVerified.postValue(false);
                error.postValue("인증 실패: " + (message == null ? "알 수 없는 오류" : message));
            }
        });
    }

    public void register(String userId, String password, String email) {
        if (loading.getValue() != null && loading.getValue()) return;
        loading.postValue(true);
        authRepository.register(new UserJoinRequest(userId, password, email), new AuthRepository.SimpleCallback() {
            @Override public void onSuccess() {
                loading.postValue(false);
                registered.postValue(true);
                status.postValue("회원가입 성공!");
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                registered.postValue(false);
                error.postValue("회원가입 실패: " + (message == null ? "알 수 없는 오류" : message));
            }
        });
    }
}
