package com.aytngr.feature.overlay

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Invisible activity whose only job is to host the MediaProjection consent dialog,
 * which a Service cannot show itself, and hand the result back to [OverlayService].
 */
@AndroidEntryPoint
class ScreenshotActivity : AppCompatActivity() {

    companion object {
        const val IS_MEDIA_PROJECTION_INITIALIZED = "IS_MEDIA_PROJECTION_INITIALIZED"

        fun start(context: Context, mediaProjection: Boolean?) {
            val intent = Intent(context, ScreenshotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(IS_MEDIA_PROJECTION_INITIALIZED, mediaProjection)
            }
            context.startActivity(intent)
        }
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            notifyService(OverlayService.ACTION_SCREENSHOT_PERMISSION_GRANTED) {
                putExtra(OverlayService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(OverlayService.EXTRA_RESULT_DATA, data)
            }
        } else {
            notifyService(OverlayService.ACTION_SCREENSHOT_PERMISSION_DENIED)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) return

        val hasProjection = intent.getBooleanExtra(IS_MEDIA_PROJECTION_INITIALIZED, false)

        val mustAsk = !hasProjection ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        if (mustAsk) {
            val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
        } else {
            notifyService(OverlayService.ACTION_SCREENSHOT_PERMISSION_GRANTED) {
                putExtra(OverlayService.EXTRA_RESULT_CODE, RESULT_OK)
            }
            finish()
        }
    }

    private fun notifyService(action: String, extras: Intent.() -> Unit = {}) {
        startService(
            Intent(this, OverlayService::class.java).apply {
                this.action = action
                extras()
            }
        )
    }
}
