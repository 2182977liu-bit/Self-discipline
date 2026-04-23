package com.example.timemanager.data.repository

import com.example.timemanager.data.local.datastore.UserPreferences
import com.example.timemanager.data.remote.api.KimiApiService
import com.example.timemanager.data.remote.dto.ChatRequest
import com.example.timemanager.data.remote.dto.Message
import com.example.timemanager.domain.model.*
import com.example.timemanager.domain.repository.AIRepository
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.first
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI仓库实现
 *
 * 实现AIRepository接口，封装Kimi API调用
 */
@Singleton
class AIRepositoryImpl @Inject constructor(
    private val kimiApiService: KimiApiService,
    private val userPreferences: UserPreferences
) : AIRepository {

    private val gson = Gson()

    override suspend fun parseTask(input: String): Result<ParsedTask> {
        return try {
            val apiKey = userPreferences.kimiApiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(IllegalStateException("请先配置API密钥"))
            }

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
                model = KimiApiService.MODEL_MOONSHOT_V1_8K,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = input)
                ),
                temperature = 0.3f,
                maxTokens = 500
            )

            val response = kimiApiService.createChatCompletion("Bearer $apiKey", request)

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content ?: ""
                parseTaskFromJson(content)
            } else {
                Result.failure(Exception("API请求失败: ${response.code()}"))
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
                model = KimiApiService.MODEL_MOONSHOT_V1_8K,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userMessage)
                ),
                temperature = 0.7f,
                maxTokens = 1000
            )

            val response = kimiApiService.createChatCompletion("Bearer $apiKey", request)

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content ?: ""
                parseSuggestionFromJson(content)
            } else {
                Result.failure(Exception("API请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun detectConflicts(tasks: List<Task>): Result<List<TimeConflict>> {
        // 简化实现：基于规则检测时间冲突
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

            // 如果两个任务时间间隔小于任务1的持续时间，则存在冲突
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
            // 提取JSON部分（处理可能的额外文本）
            val jsonStart = json.indexOf("{")
            val jsonEnd = json.lastIndexOf("}") + 1
            val jsonContent = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                json.substring(jsonStart, jsonEnd)
            } else {
                json
            }

            val jsonObject = JsonParser.parseString(jsonContent).asJsonObject

            Result.success(
                ParsedTask(
                    title = jsonObject.get("title")?.asString ?: "",
                    description = jsonObject.get("description")?.takeIf { !it.isJsonNull }?.asString,
                    dueTimeString = jsonObject.get("dueTimeString")?.takeIf { !it.isJsonNull }?.asString,
                    durationMinutes = jsonObject.get("durationMinutes")?.takeIf { !it.isJsonNull }?.asInt,
                    priority = jsonObject.get("priority")?.takeIf { !it.isJsonNull }?.asInt?.let {
                        Priority.fromValue(it - 1) // 转换为0-based
                    },
                    category = jsonObject.get("category")?.takeIf { !it.isJsonNull }?.asString
                )
            )
        } catch (e: Exception) {
            // 解析失败时返回基本任务
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

            val jsonObject = JsonParser.parseString(jsonContent).asJsonObject

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
        // 简化实现：返回null，实际应用中需要更复杂的解析逻辑
        return null
    }
}
