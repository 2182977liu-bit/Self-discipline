package com.example.timemanager.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.data.local.datastore.UserPreferences
import com.example.timemanager.domain.model.AIProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置UI状态
 */
data class SettingsUiState(
    val kimiApiKey: String = "",
    val aiProviderKey: String = "kimi",
    val customBaseUrl: String = "",
    val customModel: String = "",
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
) {
    val currentProvider: AIProvider
        get() = AIProvider.fromKey(aiProviderKey)
}

/**
 * 设置事件
 */
sealed class SettingsEvent {
    data class UpdateApiKey(val apiKey: String) : SettingsEvent()
    data object ValidateApiKey : SettingsEvent()
    data class UpdateAIProvider(val providerKey: String) : SettingsEvent()
    data class UpdateCustomBaseUrl(val url: String) : SettingsEvent()
    data class UpdateCustomModel(val model: String) : SettingsEvent()
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

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateApiKey -> updateApiKey(event.apiKey)
            is SettingsEvent.ValidateApiKey -> validateApiKey()
            is SettingsEvent.UpdateAIProvider -> updateAIProvider(event.providerKey)
            is SettingsEvent.UpdateCustomBaseUrl -> updateCustomBaseUrl(event.url)
            is SettingsEvent.UpdateCustomModel -> updateCustomModel(event.model)
            is SettingsEvent.UpdateThemeMode -> updateThemeMode(event.mode)
            is SettingsEvent.UpdateNotificationEnabled -> updateNotificationEnabled(event.enabled)
            is SettingsEvent.UpdateSoundEnabled -> updateSoundEnabled(event.enabled)
            is SettingsEvent.UpdateVibrationEnabled -> updateVibrationEnabled(event.enabled)
            is SettingsEvent.UpdateDefaultReminder -> updateDefaultReminder(event.minutes)
            is SettingsEvent.UpdateWaterReminder -> updateWaterReminder(event.enabled, event.interval)
            is SettingsEvent.ClearMessage -> clearMessage()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferences.kimiApiKey,
                userPreferences.aiProvider,
                userPreferences.customBaseUrl,
                userPreferences.customModel,
                userPreferences.themeMode
            ) { apiKey, provider, customUrl, customModel, theme ->
                arrayOf(apiKey, provider, customUrl, customModel, theme)
            }.combine(
                combine(
                    userPreferences.notificationEnabled,
                    userPreferences.soundEnabled,
                    userPreferences.vibrationEnabled
                ) { a, b, c -> arrayOf(a, b, c) }
            ) { first, mid ->
                @Suppress("UNCHECKED_CAST")
                val apiKey = first[0] as String?
                val provider = first[1] as String
                val customUrl = first[2] as String
                val customModel = first[3] as String
                val theme = first[4] as Int
                val notification = mid[0] as Boolean
                val sound = mid[1] as Boolean
                val vibration = mid[2] as Boolean
                SettingsUiState(
                    kimiApiKey = apiKey ?: "",
                    aiProviderKey = provider,
                    customBaseUrl = customUrl,
                    customModel = customModel,
                    themeMode = theme,
                    notificationEnabled = notification,
                    soundEnabled = sound,
                    vibrationEnabled = vibration,
                    defaultReminderMinutes = 30,
                    waterReminderEnabled = false,
                    waterReminderInterval = 60,
                    isApiKeyValid = if (!apiKey.isNullOrBlank()) true else null
                )
            }.combine(
                combine(
                    userPreferences.defaultReminderMinutes,
                    userPreferences.waterReminderEnabled,
                    userPreferences.waterReminderInterval
                ) { a, b, c -> arrayOf(a, b, c) }
            ) { state, last ->
                @Suppress("UNCHECKED_CAST")
                state.copy(
                    defaultReminderMinutes = last[0] as Int,
                    waterReminderEnabled = last[1] as Boolean,
                    waterReminderInterval = last[2] as Int
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            userPreferences.saveKimiApiKey(apiKey)
            _uiState.update {
                it.copy(kimiApiKey = apiKey, isApiKeyValid = null)
            }
        }
    }

    private fun validateApiKey() {
        viewModelScope.launch {
            val apiKey = _uiState.value.kimiApiKey
            if (apiKey.isBlank()) {
                _uiState.update {
                    it.copy(isApiKeyValid = false, message = "请输入API密钥")
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            val isValid = apiKey.length >= 10
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isApiKeyValid = isValid,
                    message = if (isValid) "API密钥已保存" else "API密钥格式不正确"
                )
            }
        }
    }

    private fun updateAIProvider(providerKey: String) {
        viewModelScope.launch {
            userPreferences.saveAIProvider(providerKey)
            _uiState.update { it.copy(aiProviderKey = providerKey) }
        }
    }

    private fun updateCustomBaseUrl(url: String) {
        viewModelScope.launch {
            userPreferences.saveCustomBaseUrl(url)
            _uiState.update { it.copy(customBaseUrl = url) }
        }
    }

    private fun updateCustomModel(model: String) {
        viewModelScope.launch {
            userPreferences.saveCustomModel(model)
            _uiState.update { it.copy(customModel = model) }
        }
    }

    private fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            userPreferences.saveThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    private fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.saveNotificationEnabled(enabled) }
    }

    private fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.saveSoundEnabled(enabled) }
    }

    private fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.saveVibrationEnabled(enabled) }
    }

    private fun updateDefaultReminder(minutes: Int) {
        viewModelScope.launch { userPreferences.saveDefaultReminderMinutes(minutes) }
    }

    private fun updateWaterReminder(enabled: Boolean, interval: Int) {
        viewModelScope.launch {
            userPreferences.saveWaterReminderEnabled(enabled)
            userPreferences.saveWaterReminderInterval(interval)
        }
    }

    private fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
