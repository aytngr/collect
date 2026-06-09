package com.example.data.database

import androidx.room.TypeConverter
import com.example.domain.models.TextBlock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TextBlockConverter {

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    @TypeConverter
    fun fromBlocks(list: List<TextBlock>): String = json.encodeToString(list)
    @TypeConverter
    fun toList(data: String?): List<TextBlock> = if(data.isNullOrEmpty()) emptyList() else json.decodeFromString(data)
}