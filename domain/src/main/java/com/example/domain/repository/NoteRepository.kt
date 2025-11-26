package com.example.domain.repository

import com.example.domain.models.DataResult
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    suspend fun getAllNotes(): Flow<DataResult<List<Note>>>
    suspend fun getNote(id: Long): DataResult<Note>
    suspend fun insertNote(note: Note): DataResult<Long>
    suspend fun deleteNote(note: Note): DataResult<Unit>
    suspend fun updateNote(note: Note): DataResult<Unit>
    suspend fun getNotesByCategory(category: NoteCategory): Flow<DataResult<List<Note>>>
}