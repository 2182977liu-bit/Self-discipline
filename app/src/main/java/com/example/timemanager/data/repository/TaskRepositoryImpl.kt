package com.example.timemanager.data.repository

import com.example.timemanager.data.local.database.dao.TaskDao
import com.example.timemanager.data.mapper.TaskMapper
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务仓库实现
 *
 * 实现TaskRepository接口，协调数据源
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskMapper: TaskMapper
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            taskMapper.toDomainList(entities)
        }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getTaskById(id).map { entity ->
            entity?.let { taskMapper.toDomain(it) }
        }
    }

    override fun getTasksByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Task>> {
        val startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return taskDao.getTasksByDateRange(startMillis, endMillis).map { entities ->
            taskMapper.toDomainList(entities)
        }
    }

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status.value).map { entities ->
            taskMapper.toDomainList(entities)
        }
    }

    override fun getTodayPendingTasks(): Flow<List<Task>> {
        val todayStart = LocalDateTime.now()
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return taskDao.getTodayPendingTasks(todayStart).map { entities ->
            taskMapper.toDomainList(entities)
        }
    }

    override fun searchTasks(keyword: String): Flow<List<Task>> {
        return taskDao.searchTasks(keyword).map { entities ->
            taskMapper.toDomainList(entities)
        }
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(taskMapper.toEntity(task))
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(taskMapper.toEntity(task))
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(taskMapper.toEntity(task))
    }

    override suspend fun deleteTaskById(id: String) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus) {
        taskDao.updateTaskStatus(id, status.value, System.currentTimeMillis())
    }

    override fun getTaskCount(): Flow<Int> {
        return taskDao.getTaskCount()
    }

    override fun getTodayCompletedCount(): Flow<Int> {
        val todayStart = LocalDateTime.now()
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val todayEnd = LocalDateTime.now()
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return taskDao.getTodayCompletedCount(todayStart, todayEnd)
    }
}
