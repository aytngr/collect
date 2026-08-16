package com.aytngr.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    suspend fun saveInt(key: String, value: Int)
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun saveString(key: String, value: String)
    suspend fun getInt(key: String): Flow<Int?>
    suspend fun getBoolean(key: String): Flow<Boolean?>
    suspend fun getString(key: String): Flow<String?>
}