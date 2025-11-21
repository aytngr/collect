package com.example.feature.notes

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import java.io.File

class SystemScreenshotDetector(
    private val context: Context,
    private val onScreenshotDetected: () -> Unit
) {

    private var contentObserver: ContentObserver? = null
    private var fileObserver: FileObserver? = null
    private var lastScreenshotTime = 0L

    companion object {
        private const val TAG = "ScreenshotDetector"
        private const val SCREENSHOT_DEBOUNCE_MS = 500L
    }

    fun startListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Use ContentObserver for MediaStore
            startMediaStoreObserver()
        } else {
            // Android 9 and below - Use FileObserver
            startFileObserver()
        }
        Log.d(TAG, "Screenshot detection started")
    }

    fun stopListening() {
        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
            contentObserver = null
        }

        fileObserver?.stopWatching()
        fileObserver = null

        Log.d(TAG, "Screenshot detection stopped")
    }

    /**
     * For Android 10+ (API 29+)
     * Observe MediaStore for new screenshots
     */
    private fun startMediaStoreObserver() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        val handler = Handler(Looper.getMainLooper())

        contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                uri?.let {
                    if (isScreenshotUri(it)) {
                        handleScreenshotDetected()
                    }
                }
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!
        )
    }

    /**
     * Check if URI is a screenshot
     */
    private fun isScreenshotUri(uri: Uri): Boolean {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
            )

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    )
                    val data = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    )

                    // Check if it's a screenshot based on file name or path
                    val isScreenshot = displayName?.contains("screenshot", ignoreCase = true) == true ||
                            displayName?.startsWith("Screenshot_", ignoreCase = true) == true ||
                            data?.contains("/Screenshots/", ignoreCase = true) == true ||
                            data?.contains("/DCIM/Screenshots/", ignoreCase = true) == true

                    Log.d(TAG, "Image detected: $displayName, isScreenshot: $isScreenshot")
                    return isScreenshot
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking screenshot URI: ${e.message}")
        }

        return false
    }

    /**
     * For Android 9 and below
     * Watch screenshot directory with FileObserver
     */
    private fun startFileObserver() {
        val screenshotPaths = getScreenshotDirectories()

        if (screenshotPaths.isEmpty()) {
            Log.w(TAG, "No screenshot directories found")
            return
        }

        // Watch first available screenshot directory
        val path = screenshotPaths.first()

        try {
            fileObserver = object : FileObserver(path, CREATE) {
                override fun onEvent(event: Int, path: String?) {
                    if (event == CREATE && path != null) {
                        if (path.contains("screenshot", ignoreCase = true)) {
                            Log.d(TAG, "Screenshot file created: $path")
                            handleScreenshotDetected()
                        }
                    }
                }
            }

            fileObserver?.startWatching()
            Log.d(TAG, "FileObserver watching: $path")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start FileObserver: ${e.message}")
        }
    }

    /**
     * Get common screenshot directory paths
     */
    private fun getScreenshotDirectories(): List<String> {
        val paths = mutableListOf<String>()

        // Common screenshot paths
        val possiblePaths = listOf(
            "/storage/emulated/0/Pictures/Screenshots",
            "/storage/emulated/0/DCIM/Screenshots",
            "/sdcard/Pictures/Screenshots",
            "/sdcard/DCIM/Screenshots"
        )

        possiblePaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                paths.add(path)
            }
        }

        return paths
    }

    /**
     * Handle screenshot detection with debouncing
     */
    private fun handleScreenshotDetected() {
        val currentTime = System.currentTimeMillis()

        // Debounce to avoid multiple triggers
        if (currentTime - lastScreenshotTime < SCREENSHOT_DEBOUNCE_MS) {
            return
        }

        lastScreenshotTime = currentTime

        Log.d(TAG, "System screenshot detected!")
        onScreenshotDetected()
    }
}