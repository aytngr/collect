package com.example.domain.repository

import com.example.domain.models.Language
import com.example.domain.models.VoiceRecognitionResult
import kotlinx.coroutines.flow.Flow

interface VoiceRecognitionRepository {
    fun startListening(language: Language): Flow<VoiceRecognitionResult>
    fun finishListening()
    fun stopListening()
    fun isListening(): Boolean
}