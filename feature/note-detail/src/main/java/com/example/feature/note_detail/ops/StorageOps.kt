package com.example.feature.note_detail.ops

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun Uri.copyToInternalStorage(context: Context): String? = withContext(Dispatchers.IO){
    runCatching {
        val dir = File(context.filesDir, "images").apply { if(!exists()) mkdirs() }
        val file = File(dir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(this@copyToInternalStorage)?.use {input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }.getOrNull()
}