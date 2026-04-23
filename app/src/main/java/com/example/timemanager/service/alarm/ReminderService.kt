package com.example.timemanager.service.alarm

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.timemanager.R
import com.example.timemanager.service.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 提醒服务
 *
 * 前台服务，用于处理提醒通知
 */
@AndroidEntryPoint
class ReminderService : Service() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val NOTIFICATION_ID = 9999
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "RESCHEDULE_ALARMS" -> {
                // 重新调度所有闹钟
                rescheduleAlarms()
            }
            else -> {
                // 处理任务提醒
                val taskId = intent?.getStringExtra(AlarmReceiver.EXTRA_TASK_ID)
                if (taskId != null) {
                    handleTaskReminder(taskId)
                }
            }
        }

        // 停止服务
        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * 创建前台服务通知
     */
    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("时间管理")
            .setContentText("正在处理提醒...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    /**
     * 处理任务提醒
     */
    private fun handleTaskReminder(taskId: String) {
        serviceScope.launch {
            // 从数据库获取任务并显示通知
            // 简化实现：显示通用通知
            notificationHelper.showAISuggestion("您有一个任务即将到期")
        }
    }

    /**
     * 重新调度所有闹钟
     */
    private fun rescheduleAlarms() {
        serviceScope.launch {
            // 从数据库读取所有未完成的任务，重新调度闹钟
            // 这里需要注入TaskRepository来获取任务列表
        }
    }
}
