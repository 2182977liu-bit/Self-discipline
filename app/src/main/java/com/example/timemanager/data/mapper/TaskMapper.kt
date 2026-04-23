package com.example.timemanager.data.mapper

import com.example.timemanager.data.local.database.entity.TaskEntity
import com.example.timemanager.domain.model.Priority
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * 任务数据映射器
 *
 * 负责Entity和Domain模型之间的转换
 */
class TaskMapper @Inject constructor() {

    /**
     * 将Entity转换为Domain模型
     */
    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            dueTime = entity.dueTime?.let { 
                LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(it),
                    ZoneId.systemDefault()
                )
            },
            duration = Duration.ofMinutes(entity.durationMinutes.toLong()),
            priority = Priority.fromValue(entity.priority),
            status = TaskStatus.fromValue(entity.status),
            category = entity.category,
            aiSuggested = entity.aiSuggested,
            reminderMinutes = entity.reminderMinutes,
            createdAt = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(entity.createdAt),
                ZoneId.systemDefault()
            ),
            updatedAt = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(entity.updatedAt),
                ZoneId.systemDefault()
            )
        )
    }

    /**
     * 将Domain模型转换为Entity
     */
    fun toEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            dueTime = domain.dueTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            durationMinutes = domain.duration.toMinutes().toInt(),
            priority = domain.priority.value,
            status = domain.status.value,
            category = domain.category,
            aiSuggested = domain.aiSuggested,
            reminderMinutes = domain.reminderMinutes,
            createdAt = domain.createdAt.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() 
                ?: System.currentTimeMillis(),
            updatedAt = domain.updatedAt.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() 
                ?: System.currentTimeMillis()
        )
    }

    /**
     * 批量转换Entity列表为Domain列表
     */
    fun toDomainList(entities: List<TaskEntity>): List<Task> {
        return entities.map { toDomain(it) }
    }

    /**
     * 批量转换Domain列表为Entity列表
     */
    fun toEntityList(domains: List<Task>): List<TaskEntity> {
        return domains.map { toEntity(it) }
    }
}
