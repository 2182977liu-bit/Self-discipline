package com.example.timemanager.di

import com.example.timemanager.data.local.datastore.UserPreferences
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 网络依赖注入模块
 *
 * 注意：API Service 现在由 AIRepositoryImpl 动态创建，
 * 因为不同的 AI 提供商需要不同的 Base URL。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // KimiApiService 和 AuthInterceptor 的提供已移除
    // AIRepositoryImpl 会根据用户选择的 AI 提供商动态创建 Retrofit 实例
}
