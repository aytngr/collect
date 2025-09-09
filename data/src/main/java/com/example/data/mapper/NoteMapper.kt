package com.example.data.mapper

import com.example.data.models.NoteEntity
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        category = NoteCategory.valueOf(category),
        language = Language.entries.first { it.code == language },
        createdAt = createdAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        category = category.name,
        language = language.code,
        createdAt = createdAt
    )
}