package com.aytngr.feature.overlay

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.aytngr.domain.models.Note
import com.aytngr.domain.scheduler.ReminderScheduler
import java.io.File

fun Bitmap.saveToStorage(context: Context): String? {
    val dir = File(context.filesDir, "images")
    if (!dir.exists()) dir.mkdirs()

    val file = File(dir, "ss_${System.currentTimeMillis()}.webp")

    val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
    }

    val saved = file.outputStream().use { out -> compress(format, 85, out) }
    if (!saved) {
        file.delete()
        return null
    }
    return file.absolutePath
}

fun ReminderScheduler.scheduleReminders(notes: List<Note>){
    val now = System.currentTimeMillis()
    notes.filter { it.reminderAt != null && it.reminderAt!! > now }
        .forEach { this.schedule(it.id, it.title, it.reminderAt!!) }
}
