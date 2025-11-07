package com.example.data.di

import com.example.data.repository.DataStoreRepositoryImpl
import com.example.data.repository.NotesRepositoryImpl
import com.example.data.repository.VoiceRecognitionRepositoryImpl
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.PreferenceRepository
import com.example.domain.repository.VoiceRecognitionRepository
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
    abstract fun bindVoiceRecognitionRepository(
        voiceRecognitionRepositoryImpl: VoiceRecognitionRepositoryImpl
    ): VoiceRecognitionRepository
}