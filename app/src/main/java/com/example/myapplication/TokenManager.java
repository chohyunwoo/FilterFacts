package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        SharedPreferences sp = null;
        try {
            // (권장) 최신 라이브러리에서는 MasterKey 사용, 여기서는 기존 MasterKeys 유지
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sp = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        // 암호화 초기화 실패 시 일반 SharedPreferences로 폴백(크래시 방지)
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        this.sharedPreferences = sp;
    }

    /** 최초 로그인 등 토큰 2개를 한 번에 저장 */
    public void saveTokens(String accessToken, String refreshToken) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    /** access 토큰만 교체 (refresh 재발급 시 ChatActivity에서 사용) */
    public void setAccessToken(String accessToken) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .apply();
    }

    /** refresh 토큰만 교체 (refresh 재발급 시 ChatActivity에서 사용) */
    public void setRefreshToken(String refreshToken) {
        sharedPreferences.edit()
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /** 헤더 값으로 바로 쓸 수 있게 "Bearer <token>" 형식 반환 (없으면 null) */
    public String getAuthorizationValue() {
        String t = getAccessToken();
        return (t == null || t.isEmpty()) ? null : "Bearer " + t;
    }

    public void clearAuthTokens() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }


    public boolean hasAccessToken() {
        String t = getAccessToken();
        return t != null && !t.isEmpty();
    }

    public boolean hasRefreshToken() {
        String t = getRefreshToken();
        return t != null && !t.isEmpty();
    }
}