package com.aytngr.data.storage

import android.content.Context
import com.aytngr.domain.storage.ImageStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageStore {
    override suspend fun delete(paths: List<String>) = withContext(Dispatchers.IO) {
        val root = File(context.filesDir, "images").canonicalFile
        paths.forEach { path ->
            val file = File(path).canonicalFile
            if (file.startsWith(root)) file.delete()
        }
    }
}