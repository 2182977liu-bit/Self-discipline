package com.example.timemanager.domain.model

import java.time.LocalDateTime

/**
 * 用户目标
 */
data class Goal(
    val id: String = "",
    val description: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)

/**
 * AI 生成的每日计划
 */
data class DailyPlan(
    val id: String = "",
    val date: String = "", // yyyy-MM-dd
    val goalId: String = "",
    val weather: String = "", // 当天天气描述
    val temperature: String = "", // 温度
    val planItems: String = "", // JSON: 计划项列表
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 计划项（DailyPlan.planItems 的 JSON 元素）
 */
data class PlanItem(
    val time: String = "", // "08:00"
    val title: String = "", // "晨跑30分钟"
    val type: PlanType = PlanType.EXERCISE,
    val duration: Int = 30, // 分钟
    val note: String = "", // 备注
    val completed: Boolean = false
)

enum class PlanType(val label: String) {
    SLEEP("睡眠"),
    WAKE_UP("起床"),
    MEAL("饮食"),
    EXERCISE("运动"),
    STUDY("学习"),
    REST("休息"),
    OTHER("其他")
}

/**
 * 打卡记录
 */
data class CheckIn(
    val id: String = "",
    val type: CheckInType = CheckInType.SLEEP_START,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val note: String = ""
)

enum class CheckInType(val label: String, val icon: String) {
    SLEEP_START("入睡", "🌙"),
    SLEEP_END("醒来", "☀️"),
    EXERCISE("运动", "🏃"),
    MEAL("吃饭", "🍽️"),
    WATER("喝水", "💧"),
    STUDY("学习", "📖")
}
