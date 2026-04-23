package com.example.timemanager.di

import android.content.Context
import com.example.timemanager.BuildConfig
import com.example.timemanager.data.local.datastore.UserPreferences
import com.example.timemanager.data.remote.api.KimiApiService
import com.example.timemanager.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络依赖注入模块
 *
 * 提供Retrofit、OkHttp和网络相关组件的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 提供OkHttpClient实例
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                // Debug模式下添加日志拦截器
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 提供KimiApiService实例
     */
    @Provides
    @Singleton
    fun provideKimiApiService(
        okHttpClient: OkHttpClient
    ): KimiApiService {
        return Retrofit.Builder()
            .baseUrl(KimiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KimiApiService::class.java)
    }

    /**
     * 提供AuthInterceptor实例
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        userPreferences: UserPreferences
    ): AuthInterceptor {
        return AuthInterceptor(userPreferences)
    }
}
