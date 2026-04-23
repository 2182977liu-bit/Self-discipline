package com.example.timemanager.service.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 屏幕使用检测（低功耗，辅助判断睡眠）
 */
class ScreenUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val usageStatsManager = context
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * 检查用户在过去 minutes 分钟内是否使用了手机
     * @return true = 手机被使用过（用户可能未入睡）
     */
    fun isScreenUsedInLastMinutes(minutes: Int): Boolean {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - minutes * 60 * 1000L

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return stats.any { it.lastTimeUsed > startTime }
    }

    /**
     * 获取最后一次使用手机的时间戳
     */
    fun getLastScreenUsageTime(): Long {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000L // 查最近24小时

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return stats.maxOfOrNull { it.lastTimeUsed } ?: 0L
    }
}
