package com.example.domain.models

data class Note(
    val id: Long = 0,
    val content: String,
    val title: String,
    val category: NoteCategory,
    val language: Language,
    val createdAt: Long = System.currentTimeMillis(),
    val extractedData: Map<String, String>,
    val orderN: Int = 0,
    val isRevealed: Boolean = false,
    val images: List<String?>? = null
)

enum class NoteCategory {
    GENERAL, SHOPPING, REMINDER, WORK
}

enum class Language(val code: String, val displayName: String) {
    TURKISH("tr-TR", "Türkçe"),
    AZERBAIJANI("az-AZ", "Azərbaycan"),
    ENGLISH("en-US", "English")
}