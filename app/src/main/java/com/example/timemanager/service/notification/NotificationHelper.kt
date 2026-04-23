package com.example.timemanager.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timemanager.MainActivity
import com.example.timemanager.R
import com.example.timemanager.domain.model.Task
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知助手
 *
 * 负责创建和显示各类通知
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {

    companion object {
        // 通知渠道ID
        const val CHANNEL_TASK_REMINDER = "task_reminder"
        const val CHANNEL_HEALTH_REMINDER = "health_reminder"
        const val CHANNEL_AI_SUGGESTION = "ai_suggestion"

        // 通知ID范围
        const val NOTIFICATION_ID_TASK_BASE = 1000
        const val NOTIFICATION_ID_HEALTH_BASE = 2000
        const val NOTIFICATION_ID_AI_BASE = 3000
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TASK_REMINDER,
                    "任务提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "任务截止时间提醒通知"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_HEALTH_REMINDER,
                    "健康提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "喝水、运动等健康提醒通知"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_AI_SUGGESTION,
                    "AI建议",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "AI智能建议通知"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * 显示任务提醒通知
     */
    fun showTaskReminder(task: Task) {
        val notificationId = NOTIFICATION_ID_TASK_BASE + task.id.hashCode()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("任务提醒: ${task.title}")
            .setContentText(task.description ?: "任务即将到期")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // 处理权限问题
        }
    }

    /**
     * 显示健康提醒通知
     */
    fun showHealthReminder(type: HealthReminderType, message: String) {
        val notificationId = NOTIFICATION_ID_HEALTH_BASE + type.ordinal

        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(type.title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // 处理权限问题
        }
    }

    /**
     * 显示AI建议通知
     */
    fun showAISuggestion(suggestion: String) {
        val notificationId = NOTIFICATION_ID_AI_BASE

        val notification = NotificationCompat.Builder(context, CHANNEL_AI_SUGGESTION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("AI建议")
            .setContentText(suggestion)
            .setStyle(NotificationCompat.BigTextStyle().bigText(suggestion))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // 处理权限问题
        }
    }

    /**
     * 取消任务通知
     */
    fun cancelTaskNotification(taskId: String) {
        val notificationId = NOTIFICATION_ID_TASK_BASE + taskId.hashCode()
        notificationManager.cancel(notificationId)
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}

/**
 * 健康提醒类型
 */
enum class HealthReminderType(val title: String) {
    WATER("喝水提醒"),
    EXERCISE("运动提醒"),
    REST("休息提醒")
}
