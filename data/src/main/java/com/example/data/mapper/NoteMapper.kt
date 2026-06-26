package com.example.data.mapper

import com.example.data.models.NoteEntity
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        title = title,
        isCategoryManual = isCategoryManual,
        category = NoteCategory.valueOf(category),
        language = Language.entries.first { it.code == language },
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images,
        ocrText = ocrText,
        sourceApp = sourceApp,
        reminderAt = reminderAt,
        orderN = orderN,
        isPinned = isPinned,
        extractedData = Json.decodeFromString(extractedData)
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        isCategoryManual = isCategoryManual,
        category = category.name,
        language = language.code,
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