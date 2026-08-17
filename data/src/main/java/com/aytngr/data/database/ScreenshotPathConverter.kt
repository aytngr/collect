package com.aytngr.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ScreenshotPathConverter {

    @TypeConverter
    fun fromList(list: List<String>?): String =
        Json.encodeToString(list ?: emptyList())

    @TypeConverter
    fun toList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return runCatching { Json.decodeFromString<List<String>>(data) }
            .getOrDefault(emptyList())
    }
}
