package com.example.timemanager.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 任务实体类
 *
 * 存储任务的所有属性信息
 *
 * @property id 任务唯一标识符
 * @property title 任务标题
 * @property description 任务描述（可选）
 * @property dueTime 截止时间戳（毫秒），null表示无截止时间
 * @property durationMinutes 预计耗时（分钟）
 * @property priority 优先级 (0=低, 1=中, 2=高, 3=紧急)
 * @property status 状态 (0=待办, 1=进行中, 2=已完成, 3=已取消)
 * @property category 分类标签
 * @property aiSuggested 是否由AI建议生成
 * @property reminderMinutes 提前多少分钟提醒，0表示不提醒
 * @property createdAt 创建时间戳
 * @property updatedAt 更新时间戳
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["dueTime"]),
        Index(value = ["status"]),
        Index(value = ["priority"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val dueTime: Long?,
    val durationMinutes: Int,
    val priority: Int,
    val status: Int,
    val category: String,
    val aiSuggested: Boolean = false,
    val reminderMinutes: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 提醒记录实体类
 *
 * 记录已触发的提醒，防止重复提醒
 *
 * @property id 提醒记录ID
 * @property taskId 关联的任务ID
 * @property reminderTime 提醒时间戳
 * @property triggered 是否已触发
 */
@Entity(
    tableName = "reminders",
    indices = [Index(value = ["taskId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String,
    val reminderTime: Long,
    val triggered: Boolean = false
)

/**
 * 健康提醒实体类
 *
 * 存储喝水、运动等健康提醒配置
 *
 * @property id 提醒ID
 * @property type 提醒类型 (0=喝水, 1=运动, 2=休息)
 * @property intervalMinutes 提醒间隔（分钟）
 * @property startTime 开始时间（小时:分钟，如 "09:00"）
 * @property endTime 结束时间（小时:分钟，如 "22:00"）
 * @property enabled 是否启用
 */
@Entity(tableName = "health_reminders")
data class HealthReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: Int,
    val intervalMinutes: Int,
    val startTime: String,
    val endTime: String,
    val enabled: Boolean = true
)
