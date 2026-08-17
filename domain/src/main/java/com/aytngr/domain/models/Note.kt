package com.aytngr.domain.models

data class Note(
    val id: Long = 0,
    val content: String ="",
    val title: String = "",
    val images: List<String> = emptyList(),

    val category: NoteCategory = NoteCategory.GENERAL,
    val isCategoryManual: Boolean = false,
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