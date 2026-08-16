package com.aytngr.data.extension

import android.util.Log
import com.aytngr.domain.models.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

suspend inline fun <T> handleResponse(crossinline block: suspend () -> T): DataResult<T> {
    try {
        return withContext(Dispatchers.IO){
            DataResult.Success(block())
        }
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

