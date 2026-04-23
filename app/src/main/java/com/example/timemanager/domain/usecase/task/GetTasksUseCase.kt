package com.example.timemanager.domain.usecase.task

import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有任务用例
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getAllTasks()
    }
}

/**
 * 根据ID获取任务用例
 */
class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(id: String): Flow<Task?> {
        return taskRepository.getTaskById(id)
    }
}

/**
 * 获取今日待办任务用例
 */
class GetTodayPendingTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getTodayPendingTasks()
    }
}

/**
 * 搜索任务用例
 */
class SearchTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(keyword: String): Flow<List<Task>> {
        if (keyword.isBlank()) {
            return taskRepository.getAllTasks()
        }
        return taskRepository.searchTasks(keyword.trim())
    }
}
