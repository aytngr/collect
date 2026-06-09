package com.example.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object Notes

@Serializable
data class NoteDetail(val noteId: Long?)