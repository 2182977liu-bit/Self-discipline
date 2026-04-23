package com.example.timemanager.data.repository

import com.example.timemanager.data.local.datastore.UserPreferences
import com.example.timemanager.data.remote.api.KimiApiService
import com.example.timemanager.data.remote.dto.ChatRequest
import com.example.timemanager.data.remote.dto.Message
import com.example.timemanager.domain.model.*
import com.example.timemanager.domain.repository.AIRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI仓库实现
 *
 * 支持多个 AI 提供商，动态根据用户配置创建 Retrofit 实例
 */
@Singleton
class AIRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : AIRepository {

    private val gson = Gson()

    /**
     * 根据当前配置获取 AI 提供商信息
     */
    private suspend fun getCurrentProvider(): AIProvider {
        val providerKey = userPreferences.aiProvider.first()
        return AIProvider.fromKey(providerKey)
    }

    /**
     * 获取当前配置的 Base URL
     */
    private suspend fun getBaseUrl(provider: AIProvider): String {
        return if (provider == AIProvider.CUSTOM) {
            val customUrl = userPreferences.customBaseUrl.first()
            if (customUrl.isBlank()) {
                "https://api.openai.com/" // 默认回退
            } else {
                if (!customUrl.endsWith("/")) "$customUrl/" else customUrl
            }
        } else {
            provider.baseUrl
        }
    }

    /**
     * 获取当前配置的模型名称
     */
    private suspend fun getModel(provider: AIProvider): String {
        return if (provider == AIProvider.CUSTOM) {
            val customModel = userPreferences.customModel.first()
            customModel.ifBlank { "gpt-3.5-turbo" }
        } else {
            provider.defaultModel
        }
    }

    /**
     * 动态创建 API Service
     */
    private suspend fun createApiService(): KimiApiService {
        val provider = getCurrentProvider()
        val baseUrl = getBaseUrl(provider)
        val apiKey = userPreferences.kimiApiKey.first() ?: ""

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                if (apiKey.isNotBlank()) {
                    request.header("Authorization", "Bearer $apiKey")
                }
                chain.proceed(request.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(KimiApiService::class.java)
    }

    override suspend fun parseTask(input: String): Result<ParsedTask> {
        return try {
            val apiKey = userPreferences.kimiApiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(IllegalStateException("请先配置API密钥"))
            }

            val provider = getCurrentProvider()
            val model = getModel(provider)
            val apiService = createApiService()

            val systemPrompt = """
                你是一个任务解析助手。请将用户的自然语言描述解析为结构化任务信息。

                请严格按照以下JSON格式返回结果，不要包含其他内容：
                {
                    "title": "任务标题",
                    "description": "任务描述（可选）",
                    "dueTimeString": "截止时间描述（如：明天下午3点）",
                    "durationMinutes": 预计耗时分钟数（数字）,
                    "priority": 优先级（1-4，1最低，4最高）,
                    "category": "分类（工作/学习/生活/健康/娱乐/其他）"
                }

                注意：
                1. title是必需的，从用户输入中提取核心任务
                2. 如果用户没有提到时间，dueTimeString设为null
                3. 如果用户没有提到耗时，durationMinutes设为null
                4. 根据任务紧急程度和重要性推断priority
            """.trimIndent()

            val request = ChatRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = input)
                ),
                temperature = 0.3f,
                maxTokens = 500
            )

            val response = apiService.createChatCompletion(request)

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content ?: ""
                parseTaskFromJson(content)
            } else {
                Result.failure(Exception("API请求失败: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestion(tasks: List<Task>, context: String): Result<AISuggestion> {
        return try {
            val apiKey = userPreferences.kimiApiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(IllegalStateException("请先配置API密钥"))
            }

            val provider = getCurrentProvider()
            val model = getModel(provider)
            val apiService = createApiService()

            val taskList = tasks.joinToString("\n") { task ->
                "- ${task.title} | 优先级: ${task.priority.label} | 截止: ${task.dueTime ?: "无"} | 状态: ${task.status.label}"
            }

            val systemPrompt = """
                你是一个时间管理助手。请根据用户的任务列表提供优化建议。

                请按照以下JSON格式返回：
                {
                    "suggestion": "主要建议内容",
                    "reasoning": "建议理由",
                    "priority": 建议优先处理的任务优先级调整（数字1-4）,
                    "estimatedTime": "建议执行时间"
                }
            """.trimIndent()

            val userMessage = """
                当前任务列表：
                $taskList

                上下文：$context

                请给出时间管理建议。
            """.trimIndent()

            val request = ChatRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userMessage)
                ),
                temperature = 0.7f,
                maxTokens = 1000
            )

            val response = apiService.createChatCompletion(request)

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content ?: ""
                parseSuggestionFromJson(content)
            } else {
                Result.failure(Exception("API请求失败: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun detectConflicts(tasks: List<Task>): Result<List<TimeConflict>> {
        val conflicts = mutableListOf<TimeConflict>()

        val tasksWithTime = tasks.filter { it.dueTime != null && it.status != TaskStatus.COMPLETED }
            .sortedBy { it.dueTime }

        for (i in 0 until tasksWithTime.size - 1) {
            val task1 = tasksWithTime[i]
            val task2 = tasksWithTime[i + 1]

            val timeDiff = java.time.Duration.between(
                task1.dueTime!!,
                task2.dueTime!!
            ).toMinutes()

            if (timeDiff < task1.duration.toMinutes()) {
                conflicts.add(
                    TimeConflict(
                        task1 = task1,
                        task2 = task2,
                        conflictType = ConflictType.OVERLAP,
                        suggestion = "建议调整\"${task1.title}\"或\"${task2.title}\"的时间"
                    )
                )
            }
        }

        return Result.success(conflicts)
    }

    override suspend fun generateDailyPlan(tasks: List<Task>): Result<AISuggestion> {
        return getSuggestion(tasks, "请生成今日任务规划")
    }

    override suspend fun isApiKeyConfigured(): Boolean {
        val apiKey = userPreferences.kimiApiKey.first()
        return !apiKey.isNullOrBlank()
    }

    // ==================== 辅助方法 ====================

    private fun parseTaskFromJson(json: String): Result<ParsedTask> {
        return try {
            val jsonStart = json.indexOf("{")
            val jsonEnd = json.lastIndexOf("}") + 1
            val jsonContent = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json.substring(jsonStart, jsonEnd)
            } else {
                json
            }

            val jsonObject = gson.fromJson(jsonContent, com.google.gson.JsonObject::class.java)

            Result.success(
                ParsedTask(
                    title = jsonObject.get("title")?.asString ?: "",
                    description = jsonObject.get("description")?.takeIf { !it.isJsonNull }?.asString,
                    dueTimeString = jsonObject.get("dueTimeString")?.takeIf { !it.isJsonNull }?.asString,
                    durationMinutes = jsonObject.get("durationMinutes")?.takeIf { !it.isJsonNull }?.asInt,
                    priority = jsonObject.get("priority")?.takeIf { !it.isJsonNull }?.asInt?.let {
                        Priority.fromValue(it - 1)
                    },
                    category = jsonObject.get("category")?.takeIf { !it.isJsonNull }?.asString
                )
            )
        } catch (e: Exception) {
            Result.success(
                ParsedTask(
                    title = json.take(50),
                    confidence = 0.3f
                )
            )
        }
    }

    private fun parseSuggestionFromJson(json: String): Result<AISuggestion> {
        return try {
            val jsonStart = json.indexOf("{")
            val jsonEnd = json.lastIndexOf("}") + 1
            val jsonContent = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json.substring(jsonStart, jsonEnd)
            } else {
                json
            }

            val jsonObject = gson.fromJson(jsonContent, com.google.gson.JsonObject::class.java)

            Result.success(
                AISuggestion(
                    type = SuggestionType.DAILY_PLAN,
                    content = jsonObject.get("suggestion")?.asString ?: "",
                    reasoning = jsonObject.get("reasoning")?.takeIf { !it.isJsonNull }?.asString,
                    suggestedPriority = jsonObject.get("priority")?.takeIf { !it.isJsonNull }?.asInt?.let {
                        Priority.fromValue(it - 1)
                    },
                    suggestedTime = jsonObject.get("estimatedTime")?.takeIf { !it.isJsonNull }?.asString?.let {
                        parseTimeString(it)
                    }
                )
            )
        } catch (e: Exception) {
            Result.success(
                AISuggestion(
                    type = SuggestionType.DAILY_PLAN,
                    content = json,
                    confidence = 0.5f
                )
            )
        }
    }

    private fun parseTimeString(timeStr: String): LocalDateTime? {
        return null
    }
}
