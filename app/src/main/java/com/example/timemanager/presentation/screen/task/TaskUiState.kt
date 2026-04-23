package com.example.timemanager.presentation.screen.task

import com.example.timemanager.domain.model.Priority
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.model.ParsedTask
import java.time.LocalDateTime

/**
 * 任务UI状态
 *
 * @property isLoading 是否正在加载
 * @property tasks 任务列表
 * @property selectedTask 选中的任务
 * @property selectedTaskIds 批量选中的任务ID
 * @property isSelectionMode 是否处于选择模式
 * @property searchQuery 搜索关键词
 * @property filterStatus 筛选状态
 * @property isAIParsing 是否正在AI解析
 * @property parsedTask AI解析结果
 * @property error 错误信息
 */
data class TaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val selectedTaskIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val searchQuery: String = "",
    val filterStatus: TaskStatus? = null,
    val isAIParsing: Boolean = false,
    val parsedTask: ParsedTask? = null,
    val error: String? = null
)

/**
 * 任务详情UI状态
 *
 * @property isLoading 是否正在加载
 * @property task 任务数据
 * @property title 标题
 * @property description 描述
 * @property dueTime 截止时间
 * @property durationMinutes 时长（分钟）
 * @property priority 优先级
 * @property category 分类
 * @property reminderMinutes 提醒时间（分钟）
 * @property isEditing 是否编辑模式
 * @property isSaving 是否正在保存
 * @property error 错误信息
 */
data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val title: String = "",
    val description: String = "",
    val dueTime: LocalDateTime? = null,
    val durationMinutes: Int = 30,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "",
    val reminderMinutes: Int = 0,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isNewTask: Boolean
        get() = task == null

    val isValid: Boolean
        get() = title.isNotBlank()
}

/**
 * 任务事件
 */
sealed class TaskEvent {
    // 任务列表事件
    data class Search(val query: String) : TaskEvent()
    data class FilterByStatus(val status: TaskStatus?) : TaskEvent()
    data object Refresh : TaskEvent()

    // 批量选择事件
    data object EnterSelectionMode : TaskEvent()
    data object ExitSelectionMode : TaskEvent()
    data class ToggleTaskSelection(val taskId: String) : TaskEvent()
    data object SelectAllTasks : TaskEvent()
    data object DeleteSelectedTasks : TaskEvent()

    // 任务详情事件
    data class LoadTask(val taskId: String) : TaskEvent()
    data class UpdateTitle(val title: String) : TaskEvent()
    data class UpdateDescription(val description: String) : TaskEvent()
    data class UpdateDueTime(val time: LocalDateTime?) : TaskEvent()
    data class UpdateDuration(val minutes: Int) : TaskEvent()
    data class UpdatePriority(val priority: Priority) : TaskEvent()
    data class UpdateCategory(val category: String) : TaskEvent()
    data class UpdateReminder(val minutes: Int) : TaskEvent()
    data object SaveTask : TaskEvent()
    data class DeleteTask(val taskId: String) : TaskEvent()

    // AI解析事件
    data class ParseWithAI(val input: String) : TaskEvent()
    data object ApplyParsedTask : TaskEvent()
    data object ClearParsedTask : TaskEvent()

    // 通用事件
    data object ClearError : TaskEvent()
}
