package com.example.timemanager.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 闹钟调度器
 *
 * 负责调度精确闹钟提醒
 */
@Singleton
class AlarmScheduler @Inject constructor(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 调度任务提醒闹钟
     *
     * @param taskId 任务ID
     * @param triggerTime 触发时间
     */
    fun scheduleTaskReminder(taskId: String, triggerTime: LocalDateTime) {
        val triggerTimeMillis = triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TASK_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(triggerTimeMillis, pendingIntent)
    }

    /**
     * 调度健康提醒闹钟
     *
     * @param type 提醒类型
     * @param triggerTimeMillis 触发时间（毫秒）
     */
    fun scheduleHealthReminder(type: Int, triggerTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HEALTH_REMINDER
            putExtra(AlarmReceiver.EXTRA_HEALTH_TYPE, type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(triggerTimeMillis, pendingIntent)
    }

    /**
     * 调度精确闹钟
     */
    private fun scheduleExactAlarm(triggerTimeMillis: Long, pendingIntent: PendingIntent) {
        // 检查时间是否已过
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            return
        }

        // Android 12+ 需要检查精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                // 降级为非精确闹钟
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    /**
     * 取消任务提醒闹钟
     */
    fun cancelTaskReminder(taskId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TASK_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * 取消健康提醒闹钟
     */
    fun cancelHealthReminder(type: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HEALTH_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * 检查是否有精确闹钟权限
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
