package com.example.timemanager.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 用户偏好设置存储
 *
 * 使用DataStore存储用户设置和API密钥
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    // ==================== Keys ====================

    private object PreferencesKeys {
        // API密钥
        val KIMI_API_KEY = stringPreferencesKey("kimi_api_key")

        // 用户设置
        val THEME_MODE = intPreferencesKey("theme_mode") // 0=跟随系统, 1=浅色, 2=深色
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        // 默认提醒时间
        val DEFAULT_REMINDER_MINUTES = intPreferencesKey("default_reminder_minutes")

        // 健康提醒设置
        val WATER_REMINDER_ENABLED = booleanPreferencesKey("water_reminder_enabled")
        val WATER_REMINDER_INTERVAL = intPreferencesKey("water_reminder_interval") // 分钟

        // 首次启动标记
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")

        // 上次同步时间
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    // ==================== API密钥 ====================

    /**
     * 获取Kimi API密钥
     */
    val kimiApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KIMI_API_KEY]
    }

    /**
     * 保存Kimi API密钥
     */
    suspend fun saveKimiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KIMI_API_KEY] = apiKey
        }
    }

    /**
     * 清除Kimi API密钥
     */
    suspend fun clearKimiApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.KIMI_API_KEY)
        }
    }

    // ==================== 主题设置 ====================

    /**
     * 获取主题模式
     * 0=跟随系统, 1=浅色, 2=深色
     */
    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: 0
    }

    /**
     * 保存主题模式
     */
    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    // ==================== 通知设置 ====================

    /**
     * 获取通知开关状态
     */
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true
    }

    /**
     * 保存通知开关状态
     */
    suspend fun saveNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
        }
    }

    /**
     * 获取声音开关状态
     */
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    /**
     * 保存声音开关状态
     */
    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    /**
     * 获取振动开关状态
     */
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    /**
     * 保存振动开关状态
     */
    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    // ==================== 默认提醒设置 ====================

    /**
     * 获取默认提醒时间（分钟）
     */
    val defaultReminderMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_REMINDER_MINUTES] ?: 30
    }

    /**
     * 保存默认提醒时间
     */
    suspend fun saveDefaultReminderMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_MINUTES] = minutes
        }
    }

    // ==================== 健康提醒设置 ====================

    /**
     * 获取喝水提醒开关状态
     */
    val waterReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATER_REMINDER_ENABLED] ?: false
    }

    /**
     * 保存喝水提醒开关状态
     */
    suspend fun saveWaterReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_REMINDER_ENABLED] = enabled
        }
    }

    /**
     * 获取喝水提醒间隔（分钟）
     */
    val waterReminderInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATER_REMINDER_INTERVAL] ?: 60
    }

    /**
     * 保存喝水提醒间隔
     */
    suspend fun saveWaterReminderInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_REMINDER_INTERVAL] = minutes
        }
    }

    // ==================== 其他设置 ====================

    /**
     * 获取首次启动标记
     */
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
    }

    /**
     * 标记已启动
     */
    suspend fun markAsLaunched() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
        }
    }

    /**
     * 获取上次同步时间
     */
    val lastSyncTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
    }

    /**
     * 保存同步时间
     */
    suspend fun saveLastSyncTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = time
        }
    }

    // ==================== 清除数据 ====================

    /**
     * 清除所有设置
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
