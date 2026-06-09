package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.models.TextBlock

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val textBlocks: List<TextBlock>,
    val title: String,
    val category: String,
    val language: String,
    val createdAt: Long,
    val extractedData: String,
    val orderN: Int = 0,
    val images: List<String?>? = null,
)