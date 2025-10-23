package com.example.myapplication.di;

import android.content.Context;

import com.example.myapplication.common.TokenManager;
import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.common.ApiService;
import com.example.myapplication.network.repository.AuthRepository;
import com.example.myapplication.network.repository.ChatRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static ApiClient provideApiClient(@ApplicationContext Context context) {
        return new ApiClient(context);
    }

    @Provides
    @Singleton
    public static Retrofit provideRetrofit(ApiClient apiClient) {
        return apiClient.getRetrofit();
    }

    @Provides
    @Singleton
    public static ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    public static TokenManager provideTokenManager(@ApplicationContext Context context) {
        return new TokenManager(context);
    }

    @Provides
    @Singleton
    public static AuthRepository provideAuthRepository(ApiClient apiClient) {
        return new AuthRepository(apiClient);
    }

    @Provides
    @Singleton
    public static ChatRepository provideChatRepository(ApiClient apiClient) {
        return new ChatRepository(apiClient);
    }
}
