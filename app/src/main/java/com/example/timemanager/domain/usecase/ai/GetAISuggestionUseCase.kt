package com.example.timemanager.domain.usecase.ai

import com.example.timemanager.domain.model.AISuggestion
import com.example.timemanager.domain.model.ParsedTask
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TimeConflict
import com.example.timemanager.domain.repository.AIRepository
import com.example.timemanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 解析自然语言任务用例
 */
class ParseTaskUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(input: String): Result<ParsedTask> {
        if (input.isBlank()) {
            return Result.failure(IllegalArgumentException("输入不能为空"))
        }
        return aiRepository.parseTask(input.trim())
    }
}

/**
 * 获取AI建议用例
 */
class GetAISuggestionUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(context: String = ""): Result<AISuggestion> {
        // 检查API密钥是否已配置
        if (!aiRepository.isApiKeyConfigured()) {
            return Result.failure(IllegalStateException("请先在设置中配置API密钥"))
        }

        // 获取当前任务列表
        val tasks = taskRepository.getTodayPendingTasks().first()

        return aiRepository.getSuggestion(tasks, context)
    }
}

/**
 * 检测时间冲突用例
 */
class DetectConflictsUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Result<List<TimeConflict>> {
        val tasks = taskRepository.getTodayPendingTasks().first()
        return aiRepository.detectConflicts(tasks)
    }
}

/**
 * 生成每日规划用例
 */
class GenerateDailyPlanUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Result<AISuggestion> {
        // 检查API密钥是否已配置
        if (!aiRepository.isApiKeyConfigured()) {
            return Result.failure(IllegalStateException("请先在设置中配置API密钥"))
        }

        val tasks = taskRepository.getTodayPendingTasks().first()
        return aiRepository.generateDailyPlan(tasks)
    }
}

/**
 * 检查AI功能是否可用用例
 */
class CheckAIAvailableUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(): Boolean {
        return aiRepository.isApiKeyConfigured()
    }
}
