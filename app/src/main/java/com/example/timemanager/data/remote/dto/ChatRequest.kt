package com.example.timemanager.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Kimi API 聊天请求
 *
 * @property model 模型名称，如 "moonshot-v1-8k"
 * @property messages 消息列表
 * @property temperature 温度参数，控制随机性 (0-2)
 * @property maxTokens 最大生成token数
 * @property stream 是否使用流式输出
 */
data class ChatRequest(
    val model: String = "moonshot-v1-8k",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    val stream: Boolean = false
)

/**
 * 聊天消息
 *
 * @property role 角色：system, user, assistant
 * @property content 消息内容
 */
data class Message(
    val role: String,
    val content: String
)

/**
 * Kimi API 聊天响应
 *
 * @property id 响应ID
 * @property object 对象类型
 * @property created 创建时间戳
 * @property model 使用的模型
 * @property choices 响应选项列表
 * @property usage token使用统计
 */
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

/**
 * 响应选项
 *
 * @property index 选项索引
 * @property message 响应消息
 * @property finishReason 完成原因
 */
data class Choice(
    val index: Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * Token使用统计
 *
 * @property promptTokens 提示token数
 * @property completionTokens 完成token数
 * @property totalTokens 总token数
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * 流式响应（SSE格式）
 *
 * @property id 响应ID
 * @property object 对象类型
 * @property created 创建时间戳
 * @property model 使用的模型
 * @property choices 流式选项列表
 */
data class ChatStreamResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<StreamChoice>
)

/**
 * 流式选项
 *
 * @property index 选项索引
 * @property delta 增量消息
 * @property finishReason 完成原因
 */
data class StreamChoice(
    val index: Int,
    val delta: DeltaMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * 增量消息（流式）
 *
 * @property role 角色
 * @property content 内容增量
 */
data class DeltaMessage(
    val role: String? = null,
    val content: String? = null
)

/**
 * AI任务解析请求
 *
 * 用于将自然语言解析为结构化任务
 */
data class TaskParseRequest(
    val userInput: String,
    val context: String? = null
)

/**
 * AI任务解析响应
 *
 * @property title 任务标题
 * @property description 任务描述
 * @property dueTime 截止时间（自然语言描述）
 * @property duration 预计耗时（分钟）
 * @property priority 建议优先级 (1-4)
 * @property category 建议分类
 * @property confidence 解析置信度 (0-1)
 */
data class TaskParseResponse(
    val title: String,
    val description: String? = null,
    val dueTime: String? = null,
    val duration: Int? = null,
    val priority: Int? = null,
    val category: String? = null,
    val confidence: Float = 0.8f
)

/**
 * AI建议响应
 *
 * @property suggestion 建议/优化建议
 * @property reasoning 推理过程
 * @property priority 调整后的优先级
 * @property estimatedTime 建议执行时间
 */
data class AISuggestionResponse(
    val suggestion: String,
    val reasoning: String? = null,
    val priority: Int? = null,
    val estimatedTime: String? = null
)
