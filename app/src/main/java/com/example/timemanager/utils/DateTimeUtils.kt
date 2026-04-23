package com.example.timemanager.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.RelativeTimeFormat
import java.util.Locale

/**
 * 日期时间工具类
 *
 * 提供常用的日期时间格式化和计算方法
 */
object DateTimeUtils {

    // 常用格式化器
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

    /**
     * 格式化日期
     */
    fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }

    /**
     * 格式化时间
     */
    fun formatTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }

    /**
     * 格式化日期时间
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }

    /**
     * 格式化短日期时间
     */
    fun formatShortDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(shortDateTimeFormatter)
    }

    /**
     * 获取相对时间描述
     *
     * @param dateTime 目标时间
     * @return 相对时间描述，如"刚刚"、"5分钟前"、"明天"等
     */
    fun getRelativeTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(now, dateTime)

        return when {
            // 未来
            duration.isNegative -> {
                val absDuration = duration.abs()
                when {
                    absDuration.toMinutes() < 1 -> "即将"
                    absDuration.toMinutes() < 60 -> "${absDuration.toMinutes()}分钟后"
                    absDuration.toHours() < 24 -> "${absDuration.toHours()}小时后"
                    absDuration.toDays() == 1L -> "明天"
                    absDuration.toDays() == 2L -> "后天"
                    absDuration.toDays() < 7 -> "${absDuration.toDays()}天后"
                    else -> formatShortDateTime(dateTime)
                }
            }
            // 过去
            else -> {
                when {
                    duration.toMinutes() < 1 -> "刚刚"
                    duration.toMinutes() < 60 -> "${duration.toMinutes()}分钟前"
                    duration.toHours() < 24 -> "${duration.toHours()}小时前"
                    duration.toDays() == 1L -> "昨天"
                    duration.toDays() == 2L -> "前天"
                    duration.toDays() < 7 -> "${duration.toDays()}天前"
                    else -> formatShortDateTime(dateTime)
                }
            }
        }
    }

    /**
     * 判断是否为今天
     */
    fun isToday(dateTime: LocalDateTime): Boolean {
        return dateTime.toLocalDate() == LocalDate.now()
    }

    /**
     * 判断是否为明天
     */
    fun isTomorrow(dateTime: LocalDateTime): Boolean {
        return dateTime.toLocalDate() == LocalDate.now().plusDays(1)
    }

    /**
     * 判断是否为昨天
     */
    fun isYesterday(dateTime: LocalDateTime): Boolean {
        return dateTime.toLocalDate() == LocalDate.now().minusDays(1)
    }

    /**
     * 获取今日开始时间
     */
    fun getTodayStart(): LocalDateTime {
        return LocalDate.now().atStartOfDay()
    }

    /**
     * 获取今日结束时间
     */
    fun getTodayEnd(): LocalDateTime {
        return LocalDate.now().atTime(23, 59, 59)
    }

    /**
     * 获取本周开始时间（周一）
     */
    fun getWeekStart(): LocalDateTime {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        return monday.atStartOfDay()
    }

    /**
     * 获取本周结束时间（周日）
     */
    fun getWeekEnd(): LocalDateTime {
        val today = LocalDate.now()
        val sunday = today.with(DayOfWeek.SUNDAY)
        return sunday.atTime(23, 59, 59)
    }

    /**
     * 解析时间字符串
     *
     * 支持格式：HH:mm, yyyy/MM/dd, yyyy/MM/dd HH:mm
     */
    fun parseDateTime(str: String): LocalDateTime? {
        return try {
            when {
                str.matches(Regex("\\d{1,2}:\\d{2}")) -> {
                    val time = LocalTime.parse(str, timeFormatter)
                    LocalDateTime.now().with(time)
                }
                str.matches(Regex("\\d{4}/\\d{1,2}/\\d{1,2}")) -> {
                    LocalDate.parse(str, dateFormatter).atStartOfDay()
                }
                str.matches(Regex("\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{2}")) -> {
                    LocalDateTime.parse(str, dateTimeFormatter)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 时间戳转LocalDateTime
     */
    fun fromTimestamp(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }

    /**
     * LocalDateTime转时间戳
     */
    fun toTimestamp(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
