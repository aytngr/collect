package com.example.data.extension

import android.util.Log
import com.example.domain.models.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

inline fun <T> handleResponse(block: () -> T): DataResult<T> {
    try {
        return DataResult.Success(block())
    } catch (e: Exception) {
        Log.e("RESPONSE", e.message ?: "Error")
        return DataResult.Error(e)
    }
}

inline fun <T, R> Flow<T>.handleFlowResponse(
    crossinline transform: suspend (T) -> R
): Flow<DataResult<R>> =
    this
        .map { value ->
            try {
                DataResult.Success(transform(value))
            } catch (e: Exception) {
                DataResult.Error(e)
            }
        }
        .catch { e ->
            Log.e("RESPONSE", e.message ?: "Error")
            emit(DataResult.Error(e))
        }

