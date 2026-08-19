package com.aytngr.data.di

import com.aytngr.data.reminder.AlarmReminderScheduler
import com.aytngr.data.repository.DataStoreRepositoryImpl
import com.aytngr.data.repository.NotesRepositoryImpl
import com.aytngr.data.storage.FileImageStore
import com.aytngr.domain.repository.NotesRepository
import com.aytngr.domain.repository.PreferenceRepository
import com.aytngr.domain.scheduler.ReminderScheduler
import com.aytngr.domain.storage.ImageStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(
        preferenceRepository: DataStoreRepositoryImpl
    ): PreferenceRepository

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        impl: AlarmReminderScheduler
    ): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindImageStore(
        impl: FileImageStore
    ): ImageStore
}