package com.example.timemanager.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.data.local.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置UI状态
 *
 * @property kimiApiKey Kimi API密钥
 * @property themeMode 主题模式 (0=跟随系统, 1=浅色, 2=深色)
 * @property notificationEnabled 通知开关
 * @property soundEnabled 声音开关
 * @property vibrationEnabled 振动开关
 * @property defaultReminderMinutes 默认提醒时间
 * @property waterReminderEnabled 喝水提醒开关
 * @property waterReminderInterval 喝水提醒间隔
 * @property isApiKeyValid API密钥是否有效
 * @property isLoading 是否正在加载
 * @property message 提示消息
 */
data class SettingsUiState(
    val kimiApiKey: String = "",
    val themeMode: Int = 0,
    val notificationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val defaultReminderMinutes: Int = 30,
    val waterReminderEnabled: Boolean = false,
    val waterReminderInterval: Int = 60,
    val isApiKeyValid: Boolean? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

/**
 * 设置事件
 */
sealed class SettingsEvent {
    data class UpdateApiKey(val apiKey: String) : SettingsEvent()
    data object ValidateApiKey : SettingsEvent()
    data class UpdateThemeMode(val mode: Int) : SettingsEvent()
    data class UpdateNotificationEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateSoundEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateVibrationEnabled(val enabled: Boolean) : SettingsEvent()
    data class UpdateDefaultReminder(val minutes: Int) : SettingsEvent()
    data class UpdateWaterReminder(val enabled: Boolean, val interval: Int) : SettingsEvent()
    data object ClearMessage : SettingsEvent()
}

/**
 * 设置ViewModel
 *
 * 管理设置页面的数据和业务逻辑
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 处理事件
     */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateApiKey -> updateApiKey(event.apiKey)
            is SettingsEvent.ValidateApiKey -> validateApiKey()
            is SettingsEvent.UpdateThemeMode -> updateThemeMode(event.mode)
            is SettingsEvent.UpdateNotificationEnabled -> updateNotificationEnabled(event.enabled)
            is SettingsEvent.UpdateSoundEnabled -> updateSoundEnabled(event.enabled)
            is SettingsEvent.UpdateVibrationEnabled -> updateVibrationEnabled(event.enabled)
            is SettingsEvent.UpdateDefaultReminder -> updateDefaultReminder(event.minutes)
            is SettingsEvent.UpdateWaterReminder -> updateWaterReminder(event.enabled, event.interval)
            is SettingsEvent.ClearMessage -> clearMessage()
        }
    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferences.kimiApiKey,
                userPreferences.themeMode,
                userPreferences.notificationEnabled,
                userPreferences.soundEnabled,
                userPreferences.vibrationEnabled,
                userPreferences.defaultReminderMinutes,
                userPreferences.waterReminderEnabled,
                userPreferences.waterReminderInterval
            ) { apiKey, theme, notification, sound, vibration, reminder, waterEnabled, waterInterval ->
                SettingsUiState(
                    kimiApiKey = apiKey ?: "",
                    themeMode = theme,
                    notificationEnabled = notification,
                    soundEnabled = sound,
                    vibrationEnabled = vibration,
                    defaultReminderMinutes = reminder,
                    waterReminderEnabled = waterEnabled,
                    waterReminderInterval = waterInterval,
                    isApiKeyValid = if (!apiKey.isNullOrBlank()) true else null
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    /**
     * 更新API密钥
     */
    private fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            userPreferences.saveKimiApiKey(apiKey)
            _uiState.update { 
                it.copy(
                    kimiApiKey = apiKey,
                    isApiKeyValid = null
                ) 
            }
        }
    }

    /**
     * 验证API密钥
     */
    private fun validateApiKey() {
        viewModelScope.launch {
            val apiKey = _uiState.value.kimiApiKey
            if (apiKey.isBlank()) {
                _uiState.update { 
                    it.copy(
                        isApiKeyValid = false,
                        message = "请输入API密钥"
                    ) 
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            // 简单验证：检查格式
            val isValid = apiKey.startsWith("sk-") && apiKey.length > 20

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isApiKeyValid = isValid,
                    message = if (isValid) "API密钥验证成功" else "API密钥格式不正确"
                ) 
            }
        }
    }

    /**
     * 更新主题模式
     */
    private fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            userPreferences.saveThemeMode(mode)
        }
    }

    /**
     * 更新通知开关
     */
    private fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveNotificationEnabled(enabled)
        }
    }

    /**
     * 更新声音开关
     */
    private fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveSoundEnabled(enabled)
        }
    }

    /**
     * 更新振动开关
     */
    private fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveVibrationEnabled(enabled)
        }
    }

    /**
     * 更新默认提醒时间
     */
    private fun updateDefaultReminder(minutes: Int) {
        viewModelScope.launch {
            userPreferences.saveDefaultReminderMinutes(minutes)
        }
    }

    /**
     * 更新喝水提醒
     */
    private fun updateWaterReminder(enabled: Boolean, interval: Int) {
        viewModelScope.launch {
            userPreferences.saveWaterReminderEnabled(enabled)
            userPreferences.saveWaterReminderInterval(interval)
        }
    }

    /**
     * 清除消息
     */
    private fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
