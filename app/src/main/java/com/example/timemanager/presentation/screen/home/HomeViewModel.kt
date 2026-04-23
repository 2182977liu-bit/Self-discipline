package com.example.timemanager.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.domain.usecase.ai.GetAISuggestionUseCase
import com.example.timemanager.domain.usecase.task.CompleteTaskUseCase
import com.example.timemanager.domain.usecase.task.GetTodayPendingTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val getAISuggestionUseCase: GetAISuggestionUseCase
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
}
