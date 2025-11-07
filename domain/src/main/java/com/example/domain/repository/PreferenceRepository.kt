package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    suspend fun saveInt(key: String, value: Int)
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun getInt(key: String): Flow<Int?>
    suspend fun getBoolean(key: String): Flow<Boolean?>
}