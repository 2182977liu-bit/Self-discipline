package com.example.timemanager.presentation.screen.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.domain.model.Priority
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.usecase.ai.ParseTaskUseCase
import com.example.timemanager.domain.usecase.task.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * 任务ViewModel
 *
 * 管理任务列表和任务详情的业务逻辑
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
    private val parseTaskUseCase: ParseTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(TaskDetailUiState())
    val detailState: StateFlow<TaskDetailUiState> = _detailState.asStateFlow()

    private val taskId: String? = savedStateHandle["taskId"]

    init {
        loadTasks()
        taskId?.let { if (it != "new") loadTask(it) }
    }

    /**
     * 处理事件
     */
    fun onEvent(event: TaskEvent) {
        when (event) {
            // 任务列表事件
            is TaskEvent.Search -> searchTasks(event.query)
            is TaskEvent.FilterByStatus -> filterByStatus(event.status)
            is TaskEvent.Refresh -> loadTasks()

            // 批量选择事件
            is TaskEvent.EnterSelectionMode -> enterSelectionMode()
            is TaskEvent.ExitSelectionMode -> exitSelectionMode()
            is TaskEvent.ToggleTaskSelection -> toggleTaskSelection(event.taskId)
            is TaskEvent.SelectAllTasks -> selectAllTasks()
            is TaskEvent.DeleteSelectedTasks -> deleteSelectedTasks()

            // 任务详情事件
            is TaskEvent.LoadTask -> loadTask(event.taskId)
            is TaskEvent.UpdateTitle -> updateTitle(event.title)
            is TaskEvent.UpdateDescription -> updateDescription(event.description)
            is TaskEvent.UpdateDueTime -> updateDueTime(event.time)
            is TaskEvent.UpdateDuration -> updateDuration(event.minutes)
            is TaskEvent.UpdatePriority -> updatePriority(event.priority)
            is TaskEvent.UpdateCategory -> updateCategory(event.category)
            is TaskEvent.UpdateReminder -> updateReminder(event.minutes)
            is TaskEvent.SaveTask -> saveTask()
            is TaskEvent.DeleteTask -> deleteTask(event.taskId)

            // AI解析事件
            is TaskEvent.ParseWithAI -> parseWithAI(event.input)
            is TaskEvent.ApplyParsedTask -> applyParsedTask()
            is TaskEvent.ClearParsedTask -> clearParsedTask()

            // 通用事件
            is TaskEvent.ClearError -> clearError()
        }
    }

    // ==================== 任务列表操作 ====================

    private fun loadTasks() {
        viewModelScope.launch {
            getTasksUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = e.message ?: "加载失败") 
                    }
                }
                .collect { tasks ->
                    _uiState.update { 
                        it.copy(tasks = tasks, isLoading = false) 
                    }
                }
        }
    }

    private fun searchTasks(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            searchTasksUseCase(query)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks) }
                }
        }
    }

    private fun filterByStatus(status: TaskStatus?) {
        viewModelScope.launch {
            _uiState.update { it.copy(filterStatus = status) }
            // TODO: 实现筛选逻辑
        }
    }

    // ==================== 批量选择操作 ====================

    private fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true) }
    }

    private fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedTaskIds = emptySet()) }
    }

    private fun toggleTaskSelection(taskId: String) {
        _uiState.update { state ->
            val newSelectedIds = if (taskId in state.selectedTaskIds) {
                state.selectedTaskIds - taskId
            } else {
                state.selectedTaskIds + taskId
            }
            state.copy(selectedTaskIds = newSelectedIds)
        }
    }

    private fun selectAllTasks() {
        _uiState.update { state ->
            state.copy(selectedTaskIds = state.tasks.map { it.id }.toSet())
        }
    }

    private fun deleteSelectedTasks() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedTaskIds
            selectedIds.forEach { taskId ->
                deleteTaskUseCase(taskId)
            }
            exitSelectionMode()
        }
    }

    // ==================== 任务详情操作 ====================

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            getTaskByIdUseCase(taskId)
                .onStart { _detailState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _detailState.update { 
                        it.copy(isLoading = false, error = e.message ?: "加载任务失败") 
                    }
                }
                .collect { task ->
                    task?.let {
                        _detailState.update { state ->
                            state.copy(
                                task = it,
                                title = it.title,
                                description = it.description ?: "",
                                dueTime = it.dueTime,
                                durationMinutes = it.duration.toMinutes().toInt(),
                                priority = it.priority,
                                category = it.category,
                                reminderMinutes = it.reminderMinutes,
                                isLoading = false,
                                isEditing = true
                            )
                        }
                    }
                }
        }
    }

    private fun updateTitle(title: String) {
        _detailState.update { it.copy(title = title) }
    }

    private fun updateDescription(description: String) {
        _detailState.update { it.copy(description = description) }
    }

    private fun updateDueTime(time: LocalDateTime?) {
        _detailState.update { it.copy(dueTime = time) }
    }

    private fun updateDuration(minutes: Int) {
        _detailState.update { it.copy(durationMinutes = minutes) }
    }

    private fun updatePriority(priority: Priority) {
        _detailState.update { it.copy(priority = priority) }
    }

    private fun updateCategory(category: String) {
        _detailState.update { it.copy(category = category) }
    }

    private fun updateReminder(minutes: Int) {
        _detailState.update { it.copy(reminderMinutes = minutes) }
    }

    private fun saveTask() {
        viewModelScope.launch {
            val state = _detailState.value
            if (!state.isValid) {
                _detailState.update { it.copy(error = "请填写任务标题") }
                return@launch
            }

            _detailState.update { it.copy(isSaving = true) }

            val task = Task(
                id = state.task?.id ?: UUID.randomUUID().toString(),
                title = state.title,
                description = state.description.takeIf { it.isNotBlank() },
                dueTime = state.dueTime,
                duration = Duration.ofMinutes(state.durationMinutes.toLong()),
                priority = state.priority,
                status = state.task?.status ?: TaskStatus.TODO,
                category = state.category,
                reminderMinutes = state.reminderMinutes,
                createdAt = state.task?.createdAt ?: LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            if (state.isEditing) {
                updateTaskUseCase(task)
            } else {
                addTaskUseCase(task)
            }

            _detailState.update { it.copy(isSaving = false) }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    // ==================== AI解析操作 ====================

    private fun parseWithAI(input: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAIParsing = true) }

            parseTaskUseCase(input)
                .onSuccess { parsed ->
                    _uiState.update { 
                        it.copy(
                            parsedTask = parsed,
                            isAIParsing = false
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isAIParsing = false,
                            error = e.message ?: "AI解析失败"
                        ) 
                    }
                }
        }
    }

    private fun applyParsedTask() {
        val parsed = _uiState.value.parsedTask ?: return

        _detailState.update { state ->
            state.copy(
                title = parsed.title,
                description = parsed.description ?: state.description,
                dueTime = state.dueTime, // TODO: 解析时间字符串
                durationMinutes = parsed.durationMinutes ?: state.durationMinutes,
                priority = parsed.priority ?: state.priority,
                category = parsed.category ?: state.category
            )
        }

        clearParsedTask()
    }

    private fun clearParsedTask() {
        _uiState.update { it.copy(parsedTask = null) }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
    }
}
