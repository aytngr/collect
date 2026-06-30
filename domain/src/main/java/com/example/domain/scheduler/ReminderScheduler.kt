package com.example.domain.scheduler

interface ReminderScheduler {
    fun schedule(noteId: Long, title: String, atMillis: Long)
    fun cancel(noteId: Long)
    fun canExact(): Boolean
}