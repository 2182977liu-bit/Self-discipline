package com.example.timemanager.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.timemanager.data.local.database.dao.TaskDao
import com.example.timemanager.service.alarm.AlarmScheduler
import com.example.timemanager.service.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * 提醒Worker
 *
 * 定期检查即将到期的任务并发送提醒
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val taskDao: TaskDao,
    private val notificationHelper: NotificationHelper,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkUpcomingTasks()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * 检查即将到期的任务
     */
    private suspend fun checkUpcomingTasks() {
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime + TimeUnit.MINUTES.toMillis(30) // 30分钟内

        val upcomingTasks = taskDao.getUpcomingTasks(currentTime, thresholdTime)

        upcomingTasks.forEach { taskEntity ->
            // 检查是否需要发送提醒
            val reminderTime = taskEntity.dueTime?.minusMinutes(taskEntity.reminderMinutes.toLong())
            if (reminderTime != null && reminderTime <= currentTime) {
                // 发送通知
                // notificationHelper.showTaskReminder(task)
            }
        }
    }

    companion object {
        const val WORK_NAME = "reminder_work"

        /**
         * 调度周期性提醒检查
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * 取消提醒检查
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

/**
 * 健康提醒Worker
 *
 * 定期发送健康提醒（喝水、运动等）
 */
@HiltWorker
class HealthReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val type = inputData.getInt("type", 0)
            sendHealthReminder(type)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendHealthReminder(type: Int) {
        val reminderType = when (type) {
            0 -> com.example.timemanager.service.notification.HealthReminderType.WATER
            1 -> com.example.timemanager.service.notification.HealthReminderType.EXERCISE
            else -> com.example.timemanager.service.notification.HealthReminderType.REST
        }

        val message = when (reminderType) {
            com.example.timemanager.service.notification.HealthReminderType.WATER -> "该喝水了！保持每天8杯水的好习惯~"
            com.example.timemanager.service.notification.HealthReminderType.EXERCISE -> "久坐伤身，起来活动一下吧！"
            com.example.timemanager.service.notification.HealthReminderType.REST -> "休息一下，让眼睛放松~"
        }

        notificationHelper.showHealthReminder(reminderType, message)
    }

    companion object {
        const val WORK_NAME_PREFIX = "health_reminder_"

        fun scheduleWaterReminder(context: Context, intervalMinutes: Long) {
            val workRequest = PeriodicWorkRequestBuilder<HealthReminderWorker>(
                repeatInterval = intervalMinutes,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).setInputData(
                androidx.work.Data.Builder()
                    .putInt("type", 0)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${WORK_NAME_PREFIX}water",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
