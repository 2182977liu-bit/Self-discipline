package com.example.timemanager.presentation.screen.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.data.remote.weather.WeatherApiService
import com.example.timemanager.domain.model.CheckIn
import com.example.timemanager.domain.model.CheckInType
import com.example.timemanager.domain.model.PlanItem
import com.example.timemanager.domain.repository.AIRepository
import com.example.timemanager.service.alarm.AlarmScheduler
import com.example.timemanager.service.notification.NotificationHelper
import com.example.timemanager.service.tracking.StepTracker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val aiRepository: AIRepository,
    private val stepTracker: StepTracker,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val planItemType = object : TypeToken<List<PlanItem>>() {}.type

    init {
        loadWeather()
        startStepTracking()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> {}
            is HomeEvent.ClearError -> clearError()
            is HomeEvent.ShowGoalInput -> showGoalInput()
            is HomeEvent.HideGoalInput -> hideGoalInput()
            is HomeEvent.UpdateGoalText -> updateGoalText(event.text)
            is HomeEvent.GeneratePlan -> generatePlan()
            is HomeEvent.RefreshWeather -> loadWeather()
            is HomeEvent.ShowCheckInDialog -> showCheckInDialog()
            is HomeEvent.HideCheckInDialog -> hideCheckInDialog()
            is HomeEvent.DoCheckIn -> doCheckIn(event.type)
            is HomeEvent.ClearPlan -> clearPlan()
        }
    }

    private fun startStepTracking() {
        stepTracker.startTracking()
        viewModelScope.launch {
            stepTracker.todaySteps.collect { steps ->
                _uiState.update { it.copy(todaySteps = steps) }
            }
        }
    }

    private fun showGoalInput() {
        _uiState.update { it.copy(showGoalInput = true) }
    }

    private fun hideGoalInput() {
        _uiState.update { it.copy(showGoalInput = false, goalText = "") }
    }

    private fun updateGoalText(text: String) {
        _uiState.update { it.copy(goalText = text) }
    }

    private fun generatePlan() {
        val goal = _uiState.value.goalText.trim()
        if (goal.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAILoading = true, error = null) }

            val weather = _uiState.value.weatherInfo
            val checkInSummary = _uiState.value.todayCheckIns.joinToString(", ") {
                "${it.type.label}@${it.timestamp.hour}:${String.format("%02d", it.timestamp.minute)}"
            }

            aiRepository.generateLifePlan(
                goal = goal,
                weather = weather.ifEmpty { "未知" },
                temperature = "未知",
                todayCheckIns = checkInSummary.ifEmpty { "无" },
                stepsToday = _uiState.value.todaySteps
            ).onSuccess { plan ->
                val items: List<PlanItem> = try {
                    gson.fromJson(plan.planItems, planItemType)
                } catch (e: Exception) {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        isAILoading = false,
                        currentGoal = goal,
                        planItems = items,
                        showGoalInput = false,
                        goalText = ""
                    )
                }
                // 为每个计划项设置闹钟提醒
                schedulePlanReminders(items)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isAILoading = false,
                        error = e.message ?: "生成计划失败"
                    )
                }
            }
        }
    }

    /**
     * 为计划项设置闹钟和通知
     */
    private fun schedulePlanReminders(items: List<PlanItem>) {
        val today = LocalDate.now()
        items.forEach { item ->
            try {
                val parts = item.time.split(":")
                if (parts.size != 2) return@forEach
                val hour = parts[0].toIntOrNull() ?: return@forEach
                val minute = parts[1].toIntOrNull() ?: return@forEach

                val triggerTime = LocalDateTime.of(today, LocalTime.of(hour, minute))

                // 只为未来的时间设置闹钟
                if (triggerTime.isAfter(LocalDateTime.now())) {
                    alarmScheduler.scheduleTaskReminder(
                        taskId = "plan_${item.time}_${item.title}",
                        triggerTime = triggerTime
                    )
                }
            } catch (_: Exception) {
                // 跳过无法解析时间的计划项
            }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            try {
                val retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl(WeatherApiService.BASE_URL)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
                val weatherApi = retrofit.create(WeatherApiService::class.java)
                val response = weatherApi.getCurrentWeather(39.9, 116.4)
                val current = response.current
                if (current != null) {
                    val info = "${current.getDescription()} ${current.temperature}°C"
                    _uiState.update { it.copy(weatherInfo = info) }
                }
            } catch (_: Exception) {
                // 天气获取失败不影响使用
            }
        }
    }

    private fun showCheckInDialog() {
        _uiState.update { it.copy(showCheckInDialog = true) }
    }

    private fun hideCheckInDialog() {
        _uiState.update { it.copy(showCheckInDialog = false) }
    }

    private fun doCheckIn(type: CheckInType) {
        val checkIn = CheckIn(
            id = UUID.randomUUID().toString(),
            type = type,
            timestamp = LocalDateTime.now()
        )
        _uiState.update {
            it.copy(todayCheckIns = it.todayCheckIns + checkIn)
        }
        // 发送打卡通知确认
        notificationHelper.showHealthReminder(
            type = when (type) {
                CheckInType.SLEEP_START -> com.example.timemanager.service.notification.HealthReminderType.REST
                CheckInType.SLEEP_END -> com.example.timemanager.service.notification.HealthReminderType.REST
                CheckInType.EXERCISE -> com.example.timemanager.service.notification.HealthReminderType.EXERCISE
                CheckInType.MEAL -> com.example.timemanager.service.notification.HealthReminderType.WATER
                CheckInType.WATER -> com.example.timemanager.service.notification.HealthReminderType.WATER
                CheckInType.STUDY -> com.example.timemanager.service.notification.HealthReminderType.REST
            },
            message = "${type.icon} ${type.label}打卡成功！"
        )
    }

    private fun clearPlan() {
        _uiState.update {
            it.copy(
                currentGoal = "",
                planItems = emptyList(),
                todayCheckIns = emptyList()
            )
        }
        notificationHelper.cancelAllNotifications()
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stepTracker.stopTracking()
    }
}
