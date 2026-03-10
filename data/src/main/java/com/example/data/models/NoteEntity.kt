package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val title: String,
    val category: String,
    val language: String,
    val createdAt: Long,
    val extractedData: String,
    val orderN: Int = 0,
    val images: List<String?>? = null,
)