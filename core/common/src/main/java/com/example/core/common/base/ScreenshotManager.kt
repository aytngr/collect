package com.example.core.common.base

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
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.PixelCopy
import android.view.WindowManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

class ScreenshotManager(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    companion object {
        const val SCREENSHOT_REQUEST_CODE = 1001
        private const val TAG = "ScreenshotManager"
    }

    fun getMediaProjection() = mediaProjection != null

    fun setupMediaProjection(resultCode: Int, data: Intent) {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        Log.d(TAG, "MediaProjection setup: ${mediaProjection != null}")
    }

    suspend fun captureScreenshot(): Bitmap? = suspendCancellableCoroutine { continuation ->
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null!")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        Log.d(TAG, "Screen dimensions: ${width}x${height}, density: $density")

        // Create bitmap to hold the screenshot
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Use ImageReader as surface
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        Log.d(TAG, "VirtualDisplay created: ${virtualDisplay != null}")

        // Use PixelCopy for reliable bitmap extraction (API 24+)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val surface = imageReader?.surface
                if (surface != null) {
                    // PixelCopy API - much more reliable!
                    PixelCopy.request(
                        surface,
                        bitmap,
                        { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                Log.d(TAG, "PixelCopy SUCCESS! Bitmap: ${bitmap.width}x${bitmap.height}")

                                // Clean up
                                cleanupCapture()

                                continuation.resume(bitmap)
                            } else {
                                Log.e(TAG, "PixelCopy FAILED with result: $copyResult")
                                cleanupCapture()

                                // Fallback to Image method
                                val fallbackBitmap = captureUsingImageReader()
                                continuation.resume(fallbackBitmap)
                            }
                        },
                        Handler(Looper.getMainLooper())
                    )
                } else {
                    Log.e(TAG, "Surface is null!")
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during capture: ${e.message}", e)
                cleanupCapture()
                continuation.resume(null)
            }
        }, 500) // Wait for display to stabilize

        continuation.invokeOnCancellation {
            cleanupCapture()
        }
    }

    private fun captureUsingImageReader(): Bitmap? {
        return try {
            val image = imageReader?.acquireLatestImage()

            if (image == null) {
                Log.e(TAG, "Image is null in fallback method!")
                cleanupCapture()
                return null
            }

            Log.d(TAG, "Image acquired: ${image.width}x${image.height}, format: ${image.format}")

            val bitmap = imageToBitmap(image)
            image.close()
            cleanupCapture()

            Log.d(TAG, "Bitmap created: ${bitmap.width}x${bitmap.height}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback method: ${e.message}", e)
            cleanupCapture()
            null
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        Log.d(TAG, "Image planes: pixelStride=$pixelStride, rowStride=$rowStride, rowPadding=$rowPadding")

        // Create bitmap with correct dimensions including padding
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )

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
        Log.d(TAG, "ScreenshotManager released")
    }
}