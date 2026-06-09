package com.example.domain.models

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TextBlock {
    abstract val id: String

    @Serializable @SerialName("plain")
    data class PlainText(override val id: String, val text: String): TextBlock()
    @Serializable @SerialName("voice")
    data class VoiceText(override val id: String,  val text: String, val audioFilePath: String = "", val durationMs: Long = 0): TextBlock()
}