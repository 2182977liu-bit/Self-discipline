package com.example.timemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * AI智能时间管理APP入口类
 *
 * 使用Hilt进行依赖注入
 *
 * @author TimeManager Team
 * @version 1.0.0
 */
@HiltAndroidApp
class TimeManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 应用初始化逻辑
    }
}
