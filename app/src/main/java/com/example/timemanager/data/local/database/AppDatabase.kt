package com.example.timemanager.data.local.database

import android.content.Context
import androidx.room.*
import com.example.timemanager.data.local.database.dao.TaskDao
import com.example.timemanager.data.local.database.dao.ReminderDao
import com.example.timemanager.data.local.database.dao.HealthReminderDao
import com.example.timemanager.data.local.database.entity.TaskEntity
import com.example.timemanager.data.local.database.entity.ReminderEntity
import com.example.timemanager.data.local.database.entity.HealthReminderEntity

/**
 * Room数据库配置类
 *
 * 定义数据库版本、实体和类型转换器
 */
@Database(
    entities = [
        TaskEntity::class,
        ReminderEntity::class,
        HealthReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun healthReminderDao(): HealthReminderDao

    companion object {
        const val DATABASE_NAME = "time_manager.db"
    }
}
