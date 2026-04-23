package com.example.timemanager.data.local.database.dao

import androidx.room.*
import com.example.timemanager.data.local.database.entity.TaskEntity
import com.example.timemanager.data.local.database.entity.ReminderEntity
import com.example.timemanager.data.local.database.entity.HealthReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * 任务数据访问对象
 *
 * 提供任务的CRUD操作和查询方法
 */
@Dao
interface TaskDao {

    // ==================== 查询操作 ====================

    /**
     * 获取所有任务，按截止时间升序排列
     */
    @Query("SELECT * FROM tasks ORDER BY dueTime ASC NULLS LAST, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * 根据ID获取单个任务
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<TaskEntity?>

    /**
     * 获取指定日期的任务
     * @param startOfDay 当天开始时间戳
     * @param endOfDay 当天结束时间戳
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE dueTime >= :startOfDay AND dueTime < :endOfDay
        ORDER BY dueTime ASC, priority DESC
    """)
    fun getTasksByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    /**
     * 获取指定状态的任务
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueTime ASC")
    fun getTasksByStatus(status: Int): Flow<List<TaskEntity>>

    /**
     * 获取今日待办任务
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE status IN (0, 1) 
        AND (dueTime IS NULL OR dueTime >= :todayStart)
        ORDER BY priority DESC, dueTime ASC
    """)
    fun getTodayPendingTasks(todayStart: Long): Flow<List<TaskEntity>>

    /**
     * 搜索任务（按标题或描述）
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :keyword || '%' 
        OR description LIKE '%' || :keyword || '%'
        ORDER BY dueTime ASC
    """)
    fun searchTasks(keyword: String): Flow<List<TaskEntity>>

    /**
     * 获取即将到期的任务（用于提醒）
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE status IN (0, 1) 
        AND dueTime IS NOT NULL 
        AND dueTime > :currentTime 
        AND dueTime <= :thresholdTime
        AND reminderMinutes > 0
        ORDER BY dueTime ASC
    """)
    suspend fun getUpcomingTasks(currentTime: Long, thresholdTime: Long): List<TaskEntity>

    // ==================== 插入操作 ====================

    /**
     * 插入单个任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    /**
     * 批量插入任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    // ==================== 更新操作 ====================

    /**
     * 更新任务
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * 更新任务状态
     */
    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: Int, updatedAt: Long)

    /**
     * 更新任务优先级
     */
    @Query("UPDATE tasks SET priority = :priority, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskPriority(taskId: String, priority: Int, updatedAt: Long)

    // ==================== 删除操作 ====================

    /**
     * 删除任务
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * 根据ID删除任务
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * 删除已完成的任务
     */
    @Query("DELETE FROM tasks WHERE status = 2")
    suspend fun deleteCompletedTasks()

    /**
     * 清空所有任务
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // ==================== 统计操作 ====================

    /**
     * 获取任务总数
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTaskCount(): Flow<Int>

    /**
     * 获取各状态任务数量
     */
    @Query("SELECT status, COUNT(*) as count FROM tasks GROUP BY status")
    fun getTaskCountByStatus(): Flow<List<StatusCount>>

    /**
     * 获取今日完成任务数
     */
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE status = 2 
        AND updatedAt >= :todayStart AND updatedAt < :todayEnd
    """)
    fun getTodayCompletedCount(todayStart: Long, todayEnd: Long): Flow<Int>
}

/**
 * 状态统计结果
 */
data class StatusCount(
    val status: Int,
    val count: Int
)

/**
 * 提醒数据访问对象
 */
@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    fun getRemindersByTaskId(taskId: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET triggered = 1 WHERE id = :reminderId")
    suspend fun markAsTriggered(reminderId: Long)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersByTaskId(taskId: String)
}

/**
 * 健康提醒数据访问对象
 */
@Dao
interface HealthReminderDao {

    @Query("SELECT * FROM health_reminders WHERE enabled = 1")
    fun getEnabledReminders(): Flow<List<HealthReminderEntity>>

    @Query("SELECT * FROM health_reminders WHERE type = :type")
    fun getReminderByType(type: Int): Flow<HealthReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: HealthReminderEntity)

    @Update
    suspend fun updateReminder(reminder: HealthReminderEntity)

    @Query("UPDATE health_reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
