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
        category = NoteCategory.valueOf(category),
        language = Language.entries.first { it.code == language },
        createdAt = createdAt,
        extractedData = Json.decodeFromString(extractedData)
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        category = category.name,
        language = language.code,
        createdAt = createdAt,
        extractedData = Json.encodeToString(extractedData)
    )
}