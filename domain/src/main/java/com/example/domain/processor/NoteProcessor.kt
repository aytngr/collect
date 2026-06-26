package com.example.domain.processor

import androidx.compose.runtime.key
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import javax.inject.Inject

class NoteProcessor @Inject constructor() {
//    fun processVoiceInput(content: String, language: Language): Note {
//        val category = detectCategory(content, language)
//        val extractedData = extractInformation(content, category, language)
//
//        return Note(
////            content = content,
//            title = "",
//            category = category,
//            language = language,
//            extractedData = extractedData,
//            timePassed = "1h",
//            textBlocks = emptyList()
//        )
//    }

    fun detectCategory(content: String, language: Language): NoteCategory{
        val lowercaseContent = content.lowercase()
        if(Regex("""https?://|www\.""").containsMatchIn(content)) return NoteCategory.SAVED
        return CategoryRules.keywords[language]?.entries
            ?.firstOrNull{(_, words) -> words.any{lowercaseContent.contains(it)}}
            ?.key ?: NoteCategory.GENERAL

    }

//    fun extractInformation(
//        content: String,
//        category: NoteCategory,
//        language: Language
//    ): Map<String, String> {
//        return when (category) {
//            NoteCategory.SHOPPING -> extractShoppingItems(content, language)
//            NoteCategory.REMINDER -> extractReminderInfo(content, language)
//            else -> emptyMap()
//        }
//    }
}