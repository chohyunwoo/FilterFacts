package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.common.TokenManager;
import com.example.myapplication.network.data.request.UserLoginRequest;
import com.example.myapplication.network.data.response.UserLoginResponse;
import com.example.myapplication.network.repository.AuthRepository;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final TokenManager tokenManager;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>(false);

    @Inject
    public LoginViewModel(@NonNull Application application,
                          AuthRepository authRepository,
                          TokenManager tokenManager) {
        super(application);
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getSuccess() { return success; }

    public void login(String userId, String password) {
        if (loading.getValue() != null && loading.getValue()) return;
        loading.postValue(true);
        error.postValue(null);
        authRepository.login(new UserLoginRequest(userId, password), new AuthRepository.LoginCallback() {
            @Override public void onSuccess(UserLoginResponse resp) {
                tokenManager.saveTokens(resp.getAccessToken(), resp.getRefreshToken());
                loading.postValue(false);
                success.postValue(true);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue(message == null ? "로그인 실패" : message);
                success.postValue(false);
            }
        });
    }
}
