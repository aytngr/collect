package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.domain.models.Language
import com.example.domain.models.VoiceRecognitionResult
import com.example.domain.repository.VoiceRecognitionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecognitionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): VoiceRecognitionRepository {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false

    override fun startListening(language: Language): Flow<VoiceRecognitionResult> = callbackFlow {
        withContext(Dispatchers.Main) {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    trySend(VoiceRecognitionResult.Error("Speech recognition not available on this device"))
                    return@withContext
                }

                // Clean up any existing recognizer
                finishListening()

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                isCurrentlyListening = true

                val recognitionListener = object : RecognitionListener {
                    override fun onReadyForSpeech(params: android.os.Bundle?) {
                        trySend(VoiceRecognitionResult.Listening)
                    }

                    override fun onBeginningOfSpeech() {
                        // User started speaking
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Volume level changed - you can use this for visual feedback
                        // For now, we'll ignore this
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Partial audio buffer - usually not needed
                    }

                    override fun onEndOfSpeech() {
                        // User stopped speaking
                    }

                    override fun onError(error: Int) {
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Unknown error"
                        }

                        isCurrentlyListening = false
                        trySend(VoiceRecognitionResult.Error(errorMessage))
                    }

                    override fun onResults(results: android.os.Bundle?) {
                        val matches =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognizedText = matches?.firstOrNull()

                        isCurrentlyListening = false

                        if (recognizedText != null && recognizedText.isNotBlank()) {
                            trySend(VoiceRecognitionResult.Success(recognizedText))
                        } else {
                            trySend(VoiceRecognitionResult.Error("No speech recognized"))
                        }
                    }

                    override fun onPartialResults(partialResults: android.os.Bundle?) {
                        // Real-time partial results - can be used for live transcription
                        val matches =
                            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = matches?.firstOrNull()
                        if (!partialText.isNullOrBlank()) {
                            // You can emit partial results here if needed
                            // trySend(VoiceRecognitionResult.Partial(partialText))
                        }
                    }

                    override fun onEvent(eventType: Int, params: android.os.Bundle?) {
                        // Additional events - usually not needed
                    }
                }

                speechRecognizer?.setRecognitionListener(recognitionListener)

                // Configure recognition intent
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
//                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    // Increase timeout for better user experience
//                    putExtra(
//                        RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
//                        3000
//                    )
//                    putExtra(
//                        RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
//                        3000
//                    )
                }

                // Start listening
                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                isCurrentlyListening = false
                trySend(
                    VoiceRecognitionResult.Error(
                        e.message ?: "Failed to start voice recognition"
                    )
                )
            }
        }

        // Clean up when flow is cancelled
        awaitClose {
            finishListening()
        }
    }

    override fun finishListening() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isCurrentlyListening = false
    }

    override fun stopListening() {
        if(isListening()) speechRecognizer?.stopListening()
    }

    override fun isListening(): Boolean = isCurrentlyListening
}