package com.example.domain.models

sealed class VoiceRecognitionResult {
    data object Idle : VoiceRecognitionResult()
    data object Listening : VoiceRecognitionResult()
    data class Success(val text: String) : VoiceRecognitionResult()
    data class Error(val message: String) : VoiceRecognitionResult()
}