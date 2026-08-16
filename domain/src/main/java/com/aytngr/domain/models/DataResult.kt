package com.aytngr.domain.models

sealed class DataResult<out T> {
    data class Success<T>(val data: T): DataResult<T>()
    data class Error(val exception: Throwable): DataResult<Nothing>()
    data class Loading(val isLoading: Boolean = true): DataResult<Nothing>()
}

inline fun <T> DataResult<T>.onSuccess(action: (value: T) -> Unit): DataResult<T> {
    if(this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (exception: Throwable) -> Unit): DataResult<T> {
    if(this is DataResult.Error) action(exception)
    return this
}

inline fun <T> DataResult<T>.onLoading(action: (isLoading: Boolean) -> Unit): DataResult<T> {
    if(this is DataResult.Loading) action(isLoading)
    return this
}