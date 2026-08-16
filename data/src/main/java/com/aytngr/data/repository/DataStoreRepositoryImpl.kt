package com.aytngr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aytngr.domain.repository.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PreferenceRepository {

    override suspend fun saveInt(key: String, value: Int) {
        context.dataStore.edit { settings ->
            settings[intPreferencesKey(key)] = value
        }
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        context.dataStore.edit { settings ->
            settings[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun saveString(key: String, value: String) {
        context.dataStore.edit { settings ->
            settings[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun getInt(key: String): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)]
        }
    }

    override suspend fun getBoolean(key: String): Flow<Boolean?> {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)]
        }
    }

    override suspend fun getString(key: String): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }
    }
}