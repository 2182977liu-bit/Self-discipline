package com.example.timemanager.di

import com.example.timemanager.data.repository.AIRepositoryImpl
import com.example.timemanager.data.repository.TaskRepositoryImpl
import com.example.timemanager.domain.repository.AIRepository
import com.example.timemanager.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository依赖注入模块
 *
 * 提供Repository接口与其实现的绑定
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * 绑定TaskRepository
     */
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    /**
     * 绑定AIRepository
     */
    @Binds
    @Singleton
    abstract fun bindAIRepository(
        aiRepositoryImpl: AIRepositoryImpl
    ): AIRepository
}
