package com.example.timemanager.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.usecase.ai.GetAISuggestionUseCase
import com.example.timemanager.domain.usecase.ai.ParseTaskUseCase
import com.example.timemanager.domain.usecase.task.AddTaskUseCase
import com.example.timemanager.domain.usecase.task.CompleteTaskUseCase
import com.example.timemanager.domain.usecase.task.GetTodayPendingTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * 首页ViewModel
 *
 * 管理首页的数据和业务逻辑
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayPendingTasksUseCase: GetTodayPendingTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val getAISuggestionUseCase: GetAISuggestionUseCase,
    private val parseTaskUseCase: ParseTaskUseCase,
    private val addTaskUseCase: AddTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    /**
     * 处理事件
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> loadTasks()
            is HomeEvent.CompleteTask -> completeTask(event.taskId)
            is HomeEvent.RequestAISuggestion -> requestAISuggestion()
            is HomeEvent.DismissAISuggestion -> dismissAISuggestion()
            is HomeEvent.ClearError -> clearError()

            // AI快速创建任务
            is HomeEvent.ShowAIInput -> showAIInput()
            is HomeEvent.HideAIInput -> hideAIInput()
            is HomeEvent.UpdateAIInput -> updateAIInput(event.text)
            is HomeEvent.SubmitAITask -> submitAITask()
            is HomeEvent.ConfirmParsedTask -> confirmParsedTask()
            is HomeEvent.DismissParsedTask -> dismissParsedTask()
        }
    }

    /**
     * 加载今日任务
     */
    private fun loadTasks() {
        viewModelScope.launch {
            getTodayPendingTasksUseCase()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载任务失败"
                        )
                    }
                }
                .collect { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            isLoading = false,
                            completedCount = tasks.count { task -> task.status == TaskStatus.COMPLETED }
                        )
                    }
                }
        }
    }

    /**
     * 完成任务
     */
    private fun completeTask(taskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(taskId)
        }
    }

    /**
     * 请求AI建议
     */
    private fun requestAISuggestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAILoading = true) }

            getAISuggestionUseCase()
                .onSuccess { suggestion ->
                    _uiState.update {
                        it.copy(
                            aiSuggestion = suggestion,
                            isAILoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isAILoading = false,
                            error = e.message ?: "获取AI建议失败"
                        )
                    }
                }
        }
    }

    /**
     * 关闭AI建议
     */
    private fun dismissAISuggestion() {
        _uiState.update { it.copy(aiSuggestion = null) }
    }

    /**
     * 清除错误
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== AI快速创建任务 ====================

    private fun showAIInput() {
        _uiState.update { it.copy(showAIInput = true) }
    }

    private fun hideAIInput() {
        _uiState.update { it.copy(showAIInput = false, aiInputText = "", parsedTask = null) }
    }

    private fun updateAIInput(text: String) {
        _uiState.update { it.copy(aiInputText = text) }
    }

    private fun submitAITask() {
        val input = _uiState.value.aiInputText.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAILoading = true) }

            parseTaskUseCase(input)
                .onSuccess { parsed ->
                    _uiState.update {
                        it.copy(
                            parsedTask = parsed,
                            isAILoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isAILoading = false,
                            error = e.message ?: "AI解析失败"
                        )
                    }
                }
        }
    }

    private fun confirmParsedTask() {
        val parsed = _uiState.value.parsedTask ?: return

        viewModelScope.launch {
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = parsed.title,
                description = parsed.description,
                dueTime = null, // TODO: 解析时间字符串
                duration = parsed.durationMinutes?.let { Duration.ofMinutes(it.toLong()) } ?: Duration.ofMinutes(30),
                priority = parsed.priority ?: com.example.timemanager.domain.model.Priority.MEDIUM,
                status = TaskStatus.TODO,
                category = parsed.category ?: "",
                reminderMinutes = 0,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            addTaskUseCase(task)
            hideAIInput()
        }
    }

    private fun dismissParsedTask() {
        _uiState.update { it.copy(parsedTask = null) }
    }
}
