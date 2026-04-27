package com.example.timemanager.presentation.navigation

/**
 * 应用屏幕路由定义
 */
sealed class Screen(val route: String) {
    /**
     * 首页
     */
    data object Home : Screen("home")

    /**
     * 任务列表
     */
    data object TaskList : Screen("tasks")

    /**
     * 任务详情
     * 参数: taskId - 任务ID，"new"表示新建
     */
    data object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }

    /**
     * 设置
     */
    data object Settings : Screen("settings")

    /**
     * AI助手
     */
    data object AIAssistant : Screen("ai")
}
