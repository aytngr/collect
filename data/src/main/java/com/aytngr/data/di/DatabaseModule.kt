package com.aytngr.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import com.aytngr.data.dao.NoteDao
import com.aytngr.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voice_notes_database"
        )

        if (context.isDebuggable()) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }

        return builder.build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    private fun Context.isDebuggable(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
