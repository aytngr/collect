package com.example.domain.models

data class Note(
    val id: Long = 0,
    val textBlocks: List<TextBlock> = listOf(),
    val title: String = "",
    val category: NoteCategory = NoteCategory.GENERAL,
    val language: Language = Language.AZERBAIJANI,
    val createdAt: Long = System.currentTimeMillis(),
    val timePassed: String = "",
    val extractedData: Map<String, String> = emptyMap(),
    val orderN: Int = 0,
    val isRevealed: Boolean = false,
    val isFav: Boolean = false,
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