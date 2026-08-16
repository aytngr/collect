package com.aytngr.domain.scheduler

interface ReminderScheduler {
    fun schedule(noteId: Long, title: String, atMillis: Long)
    fun cancel(noteId: Long)
    fun canExact(): Boolean
}