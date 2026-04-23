package com.example.timemanager.domain.model

import java.time.LocalDateTime

/**
 * AI建议领域模型
 *
 * 表示AI生成的任务建议或优化建议
 *
 * @property id 建议ID
 * @property type 建议类型
 * @property content 建议内容
 * @property reasoning 推理过程（可选）
 * @property suggestedPriority 建议优先级（可选）
 * @property suggestedTime 建议执行时间（可选）
 * @property relatedTaskIds 相关任务ID列表
 * @property confidence 置信度 (0-1)
 * @property createdAt 创建时间
 */
data class AISuggestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: SuggestionType,
    val content: String,
    val reasoning: String? = null,
    val suggestedPriority: Priority? = null,
    val suggestedTime: LocalDateTime? = null,
    val relatedTaskIds: List<String> = emptyList(),
    val confidence: Float = 0.8f,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 建议类型枚举
 */
enum class SuggestionType {
    TASK_PARSE,          // 任务解析
    PRIORITY_ADJUST,     // 优先级调整
    TIME_OPTIMIZE,       // 时间优化
    CONFLICT_DETECT,     // 冲突检测
    DAILY_PLAN,          // 每日规划
    HEALTH_REMINDER      // 健康提醒
}

/**
 * AI任务解析结果
 *
 * 从自然语言解析出的任务信息
 *
 * @property title 任务标题
 * @property description 任务描述
 * @property dueTimeString 截止时间（自然语言）
 * @property durationMinutes 预计耗时（分钟）
 * @property priority 建议优先级
 * @property category 建议分类
 * @property confidence 解析置信度
 */
data class ParsedTask(
    val title: String,
    val description: String? = null,
    val dueTimeString: String? = null,
    val durationMinutes: Int? = null,
    val priority: Priority? = null,
    val category: String? = null,
    val confidence: Float = 0.8f
)

/**
 * 时间冲突信息
 *
 * @property task1 第一个任务
 * @property task2 第二个任务
 * @property conflictType 冲突类型
 * @property suggestion 解决建议
 */
data class TimeConflict(
    val task1: Task,
    val task2: Task,
    val conflictType: ConflictType,
    val suggestion: String
)

/**
 * 冲突类型枚举
 */
enum class ConflictType {
    OVERLAP,      // 时间重叠
    TOO_CLOSE,    // 时间太近
    OVERDUE       // 已过期
}
