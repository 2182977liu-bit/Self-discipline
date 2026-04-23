package com.example.timemanager.data.remote.api

import com.example.timemanager.data.remote.dto.ChatRequest
import com.example.timemanager.data.remote.dto.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Kimi API服务接口
 *
 * Moonshot AI (Kimi) 聊天API定义
 * API文档: https://platform.moonshot.cn/docs/api/chat
 */
interface KimiApiService {

    /**
     * 创建聊天完成
     * Authorization 由 AuthInterceptor 自动添加
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    companion object {
        /**
         * Kimi API 基础URL
         */
        const val BASE_URL = "https://api.moonshot.cn/"

        /**
         * 可用模型列表
         */
        const val MODEL_MOONSHOT_V1_8K = "moonshot-v1-8k"
        const val MODEL_MOONSHOT_V1_32K = "moonshot-v1-32k"
        const val MODEL_MOONSHOT_V1_128K = "moonshot-v1-128k"
    }
}
