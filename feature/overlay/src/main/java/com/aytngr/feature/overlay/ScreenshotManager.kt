package com.aytngr.feature.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

class ScreenshotManager(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val mediaProjectionCallback: MediaProjection.Callback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            // Release resources if necessary
            if (mediaProjection != null) {
                mediaProjection?.unregisterCallback(this)
                mediaProjection = null
            }
        }
    }

    companion object {
        const val SCREENSHOT_REQUEST_CODE = 1001
        private const val TAG = "ScreenshotManager"
    }

    fun getMediaProjection() = mediaProjection != null

    fun setupMediaProjection(resultCode: Int, data: Intent) {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
    }

    suspend fun captureScreenshot(): Bitmap? = suspendCancellableCoroutine { continuation ->
        if (mediaProjection == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val handler = Handler(Looper.getMainLooper())
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(metrics)


        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        mediaProjection?.registerCallback(mediaProjectionCallback, null)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )


        handler.postDelayed({
            try {
                val image = imageReader?.acquireLatestImage()
                val bitmap = image?.use { imageToBitmap(it) }

                // Clean up everything immediately
                release()

                continuation.resume(bitmap)
            } catch (e: Exception) {
                release()
                continuation.resume(null)
            }
        }, 250L)

        continuation.invokeOnCancellation {
           release()
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        // Create bitmap with correct dimensions including padding
        val bitmap = createBitmap(image.width + rowPadding / pixelStride, image.height)

        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)

        // Crop to actual screen size (remove padding)
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }

    private fun cleanupCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    fun createThumbnail(bitmap: Bitmap, maxWidth: Int = 200, maxHeight: Int = 200): Bitmap {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Log.e(TAG, "Cannot create thumbnail from empty bitmap!")
            return bitmap
        }

        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        Log.d(TAG, "Creating thumbnail: ${bitmap.width}x${bitmap.height} -> ${newWidth}x${newHeight}")

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Log.e(TAG, "Cannot compress empty bitmap!")
            return ByteArray(0)
        }

        val stream = ByteArrayOutputStream()
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        if (!success) {
            Log.e(TAG, "Bitmap compression failed!")
            return ByteArray(0)
        }

        val bytes = stream.toByteArray()
        Log.d(TAG, "Bitmap compressed to ${bytes.size} bytes")
        return bytes
    }

    fun release() {
        cleanupCapture()
        mediaProjection?.stop()
        mediaProjection = null
    }
}