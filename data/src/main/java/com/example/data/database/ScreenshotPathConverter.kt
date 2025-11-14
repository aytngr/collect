package com.example.data.database

import androidx.room.TypeConverter

class ScreenshotPathConverter {

    @TypeConverter
    fun fromList(list: List<String?>?): String? = list?.joinToString("|")
    @TypeConverter
    fun toList(data: String?): List<String?>? = if(data.isNullOrEmpty()) emptyList() else data.split("|")
}