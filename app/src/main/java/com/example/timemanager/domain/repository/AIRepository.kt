package com.example.timemanager.domain.repository

import com.example.timemanager.domain.model.AISuggestion
import com.example.timemanager.domain.model.ParsedTask
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TimeConflict

/**
 * AI仓库接口
 *
 * 定义AI相关功能的抽象方法
 */
interface AIRepository {

    /**
     * 解析自然语言任务
     *
     * @param input 用户输入的自然语言描述
     * @return 解析结果
     */
    suspend fun parseTask(input: String): Result<ParsedTask>

    /**
     * 获取任务建议
     *
     * @param tasks 当前任务列表
     * @param context 上下文信息
     * @return AI建议
     */
    suspend fun getSuggestion(tasks: List<Task>, context: String): Result<AISuggestion>

    /**
     * 检测时间冲突
     *
     * @param tasks 任务列表
     * @return 冲突列表
     */
    suspend fun detectConflicts(tasks: List<Task>): Result<List<TimeConflict>>

    /**
     * 生成每日规划
     *
     * @param tasks 今日任务列表
     * @return 规划建议
     */
    suspend fun generateDailyPlan(tasks: List<Task>): Result<AISuggestion>

    /**
     * 检查API密钥是否已配置
     */
    suspend fun isApiKeyConfigured(): Boolean
}
