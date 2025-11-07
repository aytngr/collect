package com.example.feature.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.example.feature.notes.service.OverlayService

class ScreenshotActivity : Activity() {

    companion object {
        const val REQUEST_MEDIA_PROJECTION = 1001

        fun start(context: Context) {
            val intent = Intent(context, ScreenshotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request screenshot permission
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                // Send result back to service
                val intent = Intent(this, OverlayService::class.java).apply {
                    setAction( OverlayService.Companion.ACTION_SCREENSHOT_PERMISSION_GRANTED)
                    putExtra(OverlayService.Companion.EXTRA_RESULT_CODE, resultCode)
                    putExtra(OverlayService.Companion.EXTRA_RESULT_DATA, data)
                }
                startService(intent)
            } else {
                // Permission denied
                val intent = Intent(this, OverlayService::class.java).apply {

                    setAction( OverlayService.Companion.ACTION_SCREENSHOT_PERMISSION_DENIED)
                }
                startService(intent)
            }
        }

        finish()
    }
}