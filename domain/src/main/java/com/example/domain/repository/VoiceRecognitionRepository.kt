package com.example.domain.repository

interface VoiceRecognitionRepository {
    fun startListening(language: Language): Flow<VoiceRecognitionResult>
    fun stopListening()
    fun isListening(): Boolean
}