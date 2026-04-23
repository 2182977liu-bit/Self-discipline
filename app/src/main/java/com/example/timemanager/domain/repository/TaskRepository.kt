package com.example.timemanager.domain.repository

import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 任务仓库接口
 *
 * 定义任务数据访问的抽象方法
 */
interface TaskRepository {

    // ==================== 查询操作 ====================

    /**
     * 获取所有任务
     */
    fun getAllTasks(): Flow<List<Task>>

    /**
     * 根据ID获取任务
     */
    fun getTaskById(id: String): Flow<Task?>

    /**
     * 获取指定日期范围内的任务
     */
    fun getTasksByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Task>>

    /**
     * 获取指定状态的任务
     */
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    /**
     * 获取今日待办任务
     */
    fun getTodayPendingTasks(): Flow<List<Task>>

    /**
     * 搜索任务
     */
    fun searchTasks(keyword: String): Flow<List<Task>>

    // ==================== 写入操作 ====================

    /**
     * 添加任务
     */
    suspend fun insertTask(task: Task)

    /**
     * 更新任务
     */
    suspend fun updateTask(task: Task)

    /**
     * 删除任务
     */
    suspend fun deleteTask(task: Task)

    /**
     * 根据ID删除任务
     */
    suspend fun deleteTaskById(id: String)

    /**
     * 更新任务状态
     */
    suspend fun updateTaskStatus(id: String, status: TaskStatus)

    // ==================== 统计操作 ====================

    /**
     * 获取任务总数
     */
    fun getTaskCount(): Flow<Int>

    /**
     * 获取今日完成任务数
     */
    fun getTodayCompletedCount(): Flow<Int>
}
