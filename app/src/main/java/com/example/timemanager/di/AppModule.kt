package com.example.timemanager.di

import android.content.Context
import com.example.timemanager.data.local.datastore.UserPreferences
import com.example.timemanager.service.alarm.AlarmScheduler
import com.example.timemanager.service.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用级依赖注入模块
 *
 * 提供全局单例组件的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供UserPreferences实例
     * 用于存储用户偏好设置和API密钥
     */
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): UserPreferences {
        return UserPreferences(context)
    }

    /**
     * 提供NotificationHelper实例
     * 用于创建和显示通知
     */
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }

    /**
     * 提供AlarmScheduler实例
     * 用于调度精确闹钟提醒
     */
    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context
    ): AlarmScheduler {
        return AlarmScheduler(context)
    }
}
