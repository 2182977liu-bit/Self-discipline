package com.example.timemanager.domain.model

import java.time.Duration
import java.time.LocalDateTime

/**
 * 任务领域模型
 *
 * 表示一个任务的核心业务实体
 *
 * @property id 任务唯一标识符
 * @property title 任务标题
 * @property description 任务描述（可选）
 * @property dueTime 截止时间，null表示无截止时间
 * @property duration 预计耗时
 * @property priority 优先级
 * @property status 任务状态
 * @property category 分类标签
 * @property aiSuggested 是否由AI建议生成
 * @property reminderMinutes 提前多少分钟提醒，0表示不提醒
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueTime: LocalDateTime? = null,
    val duration: Duration = Duration.ZERO,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val category: String = "",
    val aiSuggested: Boolean = false,
    val reminderMinutes: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 判断任务是否已过期
     */
    fun isOverdue(): Boolean {
        return dueTime?.let { it.isBefore(LocalDateTime.now()) && status != TaskStatus.COMPLETED } ?: false
    }

    /**
     * 判断任务是否为今日任务
     */
    fun isToday(): Boolean {
        return dueTime?.toLocalDate()?.isEqual(LocalDateTime.now().toLocalDate()) ?: false
    }

    /**
     * 获取剩余时间
     */
    fun getRemainingTime(): Duration? {
        return dueTime?.let { Duration.between(LocalDateTime.now(), it) }
    }

    /**
     * 格式化显示时长
     */
    fun getFormattedDuration(): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            minutes > 0 -> "${minutes}分钟"
            else -> "未设置"
        }
    }
}

/**
 * 任务优先级枚举
 */
enum class Priority(val value: Int, val label: String) {
    LOW(0, "低"),
    MEDIUM(1, "中"),
    HIGH(2, "高"),
    URGENT(3, "紧急");

    companion object {
        fun fromValue(value: Int): Priority {
            return entries.find { it.value == value } ?: MEDIUM
        }
    }
}

/**
 * 任务状态枚举
 */
enum class TaskStatus(val value: Int, val label: String) {
    TODO(0, "待办"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    companion object {
        fun fromValue(value: Int): TaskStatus {
            return entries.find { it.value == value } ?: TaskStatus.TODO
        }
    }
}

/**
 * 任务分类枚举
 */
enum class TaskCategory(val label: String) {
    WORK("工作"),
    STUDY("学习"),
    LIFE("生活"),
    HEALTH("健康"),
    ENTERTAINMENT("娱乐"),
    OTHER("其他");

    companion object {
        fun fromLabel(label: String): TaskCategory {
            return entries.find { it.label == label } ?: OTHER
        }
    }
}
