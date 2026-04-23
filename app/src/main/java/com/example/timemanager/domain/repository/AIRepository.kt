package com.example.timemanager.domain.repository

import com.example.timemanager.domain.model.AISuggestion
import com.example.timemanager.domain.model.DailyPlan
import com.example.timemanager.domain.model.ParsedTask
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TimeConflict

/**
 * AI仓库接口
 */
interface AIRepository {

    suspend fun parseTask(input: String): Result<ParsedTask>
    suspend fun getSuggestion(tasks: List<Task>, context: String): Result<AISuggestion>
    suspend fun detectConflicts(tasks: List<Task>): Result<List<TimeConflict>>
    suspend fun generateDailyPlan(tasks: List<Task>): Result<AISuggestion>
    suspend fun isApiKeyConfigured(): Boolean

    /**
     * 根据用户目标生成每日计划（含天气上下文）
     */
    suspend fun generateLifePlan(
        goal: String,
        weather: String,
        temperature: String,
        todayCheckIns: String,
        stepsToday: Int
    ): Result<DailyPlan>
}
