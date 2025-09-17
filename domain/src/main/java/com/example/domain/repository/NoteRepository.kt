package com.example.domain.repository

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(): Flow<DataResult<List<Note>>>
    suspend fun insertNote(note: Note): DataResult<Long>
    suspend fun deleteNote(note: Note): DataResult<Unit>
    suspend fun updateNote(note: Note)
    fun getNotesByCategory(category: NoteCategory): Flow<DataResult<List<Note>>>
}