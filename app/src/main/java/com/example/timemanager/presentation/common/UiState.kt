package com.example.timemanager.presentation.common

/**
 * UI状态封装
 *
 * 用于表示UI加载状态的通用密封类
 */
sealed class UiState<out T> {
    /**
     * 初始状态
     */
    data object Initial : UiState<Nothing>()

    /**
     * 加载中
     */
    data object Loading : UiState<Nothing>()

    /**
     * 成功状态
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 错误状态
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * 空数据状态
     */
    data object Empty : UiState<Nothing>()

    /**
     * 是否正在加载
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 是否错误
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 获取数据
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * 获取错误信息
     */
    fun getErrorMessage(): String? = (this as? Error)?.message

    /**
     * 映射数据
     */
    inline fun <R> map(transform: (T) -> R): UiState<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Loading -> Loading
            is Error -> Error(message)
            is Empty -> Empty
            is Initial -> Initial
        }
    }

    /**
     * 成功时执行
     */
    inline fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 错误时执行
     */
    inline fun onError(action: (String) -> Unit): UiState<T> {
        if (this is Error) action(message)
        return this
    }

    /**
     * 加载时执行
     */
    inline fun onLoading(action: () -> Unit): UiState<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * 通用UI事件
 */
sealed class UiEvent {
    /**
     * 显示Toast
     */
    data class ShowToast(val message: String) : UiEvent()

    /**
     * 显示Snackbar
     */
    data class ShowSnackbar(val message: String, val action: String? = null) : UiEvent()

    /**
     * 导航返回
     */
    data object NavigateBack : UiEvent()

    /**
     * 导航到指定路由
     */
    data class NavigateTo(val route: String) : UiEvent()
}
