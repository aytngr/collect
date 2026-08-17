package com.aytngr.data.mapper

import com.aytngr.data.models.NoteEntity
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.NoteCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        title = title,
        isCategoryManual = isCategoryManual,
        category = NoteCategory.entries.firstOrNull { it.name == category }
            ?: NoteCategory.GENERAL,
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images,
        ocrText = ocrText,
        sourceApp = sourceApp,
        reminderAt = reminderAt,
        orderN = orderN,
        isPinned = isPinned,
        extractedData = runCatching {
            Json.decodeFromString<Map<String, String>>(extractedData)
        }.getOrDefault(emptyMap())
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        isCategoryManual = isCategoryManual,
        category = category.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images,
        sourceApp = sourceApp,
        reminderAt = reminderAt,
        ocrText = ocrText,
        orderN = orderN,
        isPinned = isPinned,
        extractedData = Json.encodeToString(extractedData)
    )
}
