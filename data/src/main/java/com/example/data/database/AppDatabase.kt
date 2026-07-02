package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.NoteDao
import com.example.data.models.NoteEntity

@Database(entities = [NoteEntity::class], version = 9, exportSchema = true)
@TypeConverters(ScreenshotPathConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}