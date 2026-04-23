package com.example.timemanager.di

import android.content.Context
import androidx.room.Room
import com.example.timemanager.data.local.database.AppDatabase
import com.example.timemanager.data.local.database.dao.TaskDao
import com.example.timemanager.data.local.database.dao.ReminderDao
import com.example.timemanager.data.local.database.dao.HealthReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 *
 * 提供Room数据库和相关DAO的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供AppDatabase实例
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 开发阶段使用，生产环境应使用迁移策略
            .build()
    }

    /**
     * 提供TaskDao实例
     */
    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    /**
     * 提供ReminderDao实例
     */
    @Provides
    @Singleton
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    /**
     * 提供HealthReminderDao实例
     */
    @Provides
    @Singleton
    fun provideHealthReminderDao(database: AppDatabase): HealthReminderDao {
        return database.healthReminderDao()
    }
}
