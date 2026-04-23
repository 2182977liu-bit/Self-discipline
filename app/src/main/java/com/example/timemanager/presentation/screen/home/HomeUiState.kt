package com.example.timemanager.presentation.screen.home

import com.example.timemanager.domain.model.CheckIn
import com.example.timemanager.domain.model.CheckInType
import com.example.timemanager.domain.model.PlanItem

/**
 * 首页UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isAILoading: Boolean = false,
    val error: String? = null,

    // 目标
    val currentGoal: String = "",
    val showGoalInput: Boolean = false,
    val goalText: String = "",

    // 今日计划
    val planItems: List<PlanItem> = emptyList(),

    // 天气
    val weatherInfo: String = "",

    // 打卡
    val todayCheckIns: List<CheckIn> = emptyList(),
    val showCheckInDialog: Boolean = false,

    // 步数
    val todaySteps: Int = 0
)

/**
 * 首页事件
 */
sealed class HomeEvent {
    data object Refresh : HomeEvent()
    data object ClearError : HomeEvent()

    // 目标与计划
    data object ShowGoalInput : HomeEvent()
    data object HideGoalInput : HomeEvent()
    data class UpdateGoalText(val text: String) : HomeEvent()
    data object GeneratePlan : HomeEvent()

    // 天气
    data object RefreshWeather : HomeEvent()

    // 打卡
    data object ShowCheckInDialog : HomeEvent()
    data object HideCheckInDialog : HomeEvent()
    data class DoCheckIn(val type: CheckInType) : HomeEvent()
}
