package com.example.domain.processor

import androidx.compose.runtime.key
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import javax.inject.Inject

class NoteProcessor @Inject constructor() {
    fun processVoiceInput(content: String, language: Language): Note {
        val category = detectCategory(content, language)
        val extractedData = extractInformation(content, category, language)

        return Note(
            content = content,
            category = category,
            language = language,
            extractedData = extractedData
        )
    }

    fun detectCategory(content: String, language: Language): NoteCategory{
        val lowercaseContent = content.lowercase()

        val keywords = when(language){
            Language.TURKISH -> mapOf(
                NoteCategory.SHOPPING to listOf("al", "satın al", "market", "alışveriş", "buy"),
                NoteCategory.REMINDER to listOf("hatırlat", "unutma", "hatırla", "remind"),
                NoteCategory.WORK to listOf("toplantı", "arama", "iş", "çalışma", "meeting")
            )
            Language.AZERBAIJANI -> mapOf(
                NoteCategory.SHOPPING to listOf("al", "market", "mağaza", "bazar", "alış-veriş"),
                NoteCategory.REMINDER to listOf("xatırlat", "unutma", "yada sal"),
                NoteCategory.WORK to listOf("görüş", "zəng", "iş", "işləmək")
            )
            Language.ENGLISH -> mapOf(
                NoteCategory.SHOPPING to listOf("buy", "get", "pick up", "shopping", "store"),
                NoteCategory.REMINDER to listOf("remind", "remember", "don't forget"),
                NoteCategory.WORK to listOf("meeting", "call", "work", "project", "deadline")
            )
        }

        return keywords.entries.firstOrNull { (_, keywordList) ->
            keywordList.any { keyword -> lowercaseContent.contains(keyword)}
        }?.key ?: NoteCategory.GENERAL
    }

    fun extractInformation(
        content: String,
        category: NoteCategory,
        language: Language
    ): Map<String, String> {
        return when (category) {
            NoteCategory.SHOPPING -> extractShoppingItems(content, language)
            NoteCategory.REMINDER -> extractReminderInfo(content, language)
            else -> emptyMap()
        }
    }

    fun extractShoppingItems(content: String, language: Language): Map<String, String> {
        val items = content.split(Regex("[,ve and]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString { ", " }

        return mapOf("items" to items)
    }

    private fun extractReminderInfo(content: String, language: Language): Map<String, String> {
        val timeRegex = Regex("""(\d{1,2}:\d{2}|\d{1,2} (saat|o'clock|saatda))""")
        val timeMatch = timeRegex.find(content)

        return if (timeMatch != null) {
            mapOf("time" to timeMatch.value)
        } else {
            emptyMap()
        }
    }
}