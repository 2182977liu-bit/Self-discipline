package com.example.timemanager.data.remote.interceptor

import com.example.timemanager.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证拦截器
 *
 * 自动为请求添加Authorization头
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 获取API密钥
        val apiKey = runBlocking {
            userPreferences.kimiApiKey.first()
        }

        // 如果没有API密钥，直接发送原请求
        if (apiKey.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // 添加Authorization头
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
