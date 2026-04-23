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
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun healthReminderDao(): HealthReminderDao

    companion object {
        const val DATABASE_NAME = "time_manager.db"
    }
}

/**
 * Room类型转换器
 *
 * 用于转换复杂类型到数据库基本类型
 */
class Converters {

    // 如需添加其他类型转换，可在此扩展
    // 例如：List<String> <-> String (JSON)
}
