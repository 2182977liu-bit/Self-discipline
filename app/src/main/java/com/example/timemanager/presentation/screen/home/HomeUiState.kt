package com.example.timemanager.presentation.screen.home

import com.example.timemanager.domain.model.AISuggestion
import com.example.timemanager.domain.model.ParsedTask
import com.example.timemanager.domain.model.Task

/**
 * 首页UI状态
 *
 * @property isLoading 是否正在加载
 * @property tasks 今日任务列表
 * @property aiSuggestion AI建议
 * @property completedCount 今日已完成任务数
 * @property error 错误信息
 * @property isAILoading 是否正在AI处理
 * @property showAIInput 是否显示AI输入框
 * @property aiInputText AI输入文本
 * @property parsedTask AI解析结果
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val aiSuggestion: AISuggestion? = null,
    val completedCount: Int = 0,
    val error: String? = null,
    val isAILoading: Boolean = false,
    val showAIInput: Boolean = false,
    val aiInputText: String = "",
    val parsedTask: ParsedTask? = null
) {
    /**
     * 待办任务数量
     */
    val pendingCount: Int
        get() = tasks.count { it.status != com.example.timemanager.domain.model.TaskStatus.COMPLETED }

    /**
     * 是否有任务
     */
    val hasTasks: Boolean
        get() = tasks.isNotEmpty()

    /**
     * 是否显示AI建议
     */
    val showAISuggestion: Boolean
        get() = aiSuggestion != null && !isAILoading
}

/**
 * 首页事件
 */
sealed class HomeEvent {
    /**
     * 刷新数据
     */
    data object Refresh : HomeEvent()

    /**
     * 完成任务
     */
    data class CompleteTask(val taskId: String) : HomeEvent()

    /**
     * 请求AI建议
     */
    data object RequestAISuggestion : HomeEvent()

    /**
     * 关闭AI建议
     */
    data object DismissAISuggestion : HomeEvent()

    /**
     * 清除错误
     */
    data object ClearError : HomeEvent()

    // AI快速创建任务事件
    data object ShowAIInput : HomeEvent()
    data object HideAIInput : HomeEvent()
    data class UpdateAIInput(val text: String) : HomeEvent()
    data object SubmitAITask : HomeEvent()
    data object ConfirmParsedTask : HomeEvent()
    data object DismissParsedTask : HomeEvent()
}
