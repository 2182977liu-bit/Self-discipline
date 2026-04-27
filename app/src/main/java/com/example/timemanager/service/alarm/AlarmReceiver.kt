package com.example.timemanager.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.timemanager.service.notification.HealthReminderType
import com.example.timemanager.service.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 闹钟接收器
 *
 * 接收闹钟广播并触发提醒
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.timemanager.ACTION_TASK_REMINDER"
        const val ACTION_HEALTH_REMINDER = "com.example.timemanager.ACTION_HEALTH_REMINDER"
        const val ACTION_BOOT_COMPLETED = "com.example.timemanager.ACTION_BOOT_COMPLETED"

        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_HEALTH_TYPE = "health_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TASK_REMINDER -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                if (taskId != null) {
                    handleTaskReminder(context, taskId)
                }
            }

            ACTION_HEALTH_REMINDER -> {
                val type = intent.getIntExtra(EXTRA_HEALTH_TYPE, 0)
                handleHealthReminder(type)
            }
        }
    }

    /**
     * 处理任务提醒
     */
    private fun handleTaskReminder(context: Context, taskId: String) {
        // 启动前台服务处理通知（Android 8+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceIntent = Intent(context, ReminderService::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            // 直接显示通知
            showTaskNotification(taskId)
        }
    }

    /**
     * 处理健康提醒
     */
    private fun handleHealthReminder(type: Int) {
        val reminderType = when (type) {
            0 -> HealthReminderType.WATER
            1 -> HealthReminderType.EXERCISE
            2 -> HealthReminderType.REST
            else -> HealthReminderType.WATER
        }

        val message = when (reminderType) {
            HealthReminderType.WATER -> "该喝水了！保持每天8杯水的好习惯~"
            HealthReminderType.EXERCISE -> "久坐伤身，起来活动一下吧！"
            HealthReminderType.REST -> "休息一下，让眼睛放松~"
        }

        notificationHelper.showHealthReminder(reminderType, message)
    }

    /**
     * 显示任务通知
     */
    private fun showTaskNotification(taskId: String) {
        val pendingResult = goAsync()
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                notificationHelper.showAISuggestion("您有一个任务即将到期")
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * 开机启动接收器
 *
 * 设备启动后重新调度闹钟
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 重新调度所有闹钟
            // 这里需要启动WorkManager或Service来重新调度
            val serviceIntent = Intent(context, ReminderService::class.java).apply {
                action = "RESCHEDULE_ALARMS"
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
