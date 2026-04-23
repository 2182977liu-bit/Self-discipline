package com.example.timemanager.domain.usecase.task

import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.repository.TaskRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * 添加任务用例
 */
class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        val newTask = task.copy(
            id = if (task.id.isBlank()) UUID.randomUUID().toString() else task.id,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        taskRepository.insertTask(newTask)
    }
}

/**
 * 更新任务用例
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        val updatedTask = task.copy(
            updatedAt = LocalDateTime.now()
        )
        taskRepository.updateTask(updatedTask)
    }
}

/**
 * 完成任务用例
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
    }
}

/**
 * 删除任务用例
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.deleteTaskById(taskId)
    }
}

/**
 * 取消任务用例
 */
class CancelTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.updateTaskStatus(taskId, TaskStatus.CANCELLED)
    }
}
