package com.example.domain.models

data class Note(
    val id: Long = 0,
    val content: String ="",
    val title: String = "",
    val images: List<String?>? = null,

    val category: NoteCategory = NoteCategory.GENERAL,
    val isCategoryManual: Boolean = false,
    val language: Language = Language.AZERBAIJANI,
    val extractedData: Map<String, String> = emptyMap(),

    val ocrText: String = "",
    val sourceApp: String? = null,

    val reminderAt: Long? = null,


    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val orderN: Int = 0,
    val isPinned: Boolean = false,

//    val isRevealed: Boolean = false
)

enum class NoteCategory {
    GENERAL, SHOPPING, FINANCE, WORK, SAVED
}

enum class Language(val code: String, val displayName: String) {
    TURKISH("tr-TR", "Türkçe"),
    AZERBAIJANI("az-AZ", "Azərbaycan"),
    ENGLISH("en-US", "English")
}