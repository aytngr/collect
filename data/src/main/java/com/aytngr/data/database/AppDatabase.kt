package com.aytngr.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aytngr.data.dao.NoteDao
import com.aytngr.data.models.NoteEntity

@Database(entities = [NoteEntity::class], version = 10, exportSchema = true)
@TypeConverters(ScreenshotPathConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}