package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String = "",
    val title: String = "",
    val images: List<String?>? = null,

    val category: String,
    val isCategoryManual: Boolean = false,
    val extractedData: String,

    val ocrText: String = "",
    val sourceApp: String? = null,

    val reminderAt: Long? = null,


    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val orderN: Int = 0,
    val isPinned: Boolean = false,
)