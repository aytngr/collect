package com.example.feature.overlay

import android.content.Context
import android.graphics.Bitmap
import java.io.File

fun Bitmap?.saveToStorage(context: Context): String {
    val dir = File(context.filesDir, "images")
    if (!dir.exists()) dir.mkdirs()

    val filename = "ss_${System.currentTimeMillis()}.png"
    val file = File(dir, filename)

    file.outputStream().use { out ->
        this?.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    return file.absolutePath
}
