package com.aytngr.feature.overlay

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aytngr.domain.usecase.GetPermissionDataUseCase
import com.aytngr.domain.usecase.SavePermissionDataUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotActivity : AppCompatActivity() {

    @Inject
    lateinit var savePermissionDataUseCase: SavePermissionDataUseCase

    @Inject
    lateinit var getPermissionDataUseCase: GetPermissionDataUseCase

    companion object {
        const val REQUEST_MEDIA_PROJECTION = 1001
        const val IS_MEDIA_PROJECTION_INITIALIZED = "IS_MEDIA_PROJECTION_INITIALIZED"

        fun start(context: Context, mediaProjection: Boolean?) {
            val intent = Intent(context, ScreenshotActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(IS_MEDIA_PROJECTION_INITIALIZED, mediaProjection)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mediaProjection = intent.getBooleanExtra(IS_MEDIA_PROJECTION_INITIALIZED, false)

        if (!mediaProjection) {
            val mediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
            )
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                    val mediaProjectionManager =
                        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION
                    )
                }else{
                    val intent = Intent(this@ScreenshotActivity, OverlayService::class.java).apply {
                        setAction(OverlayService.Companion.ACTION_SCREENSHOT_PERMISSION_GRANTED)
                        putExtra(OverlayService.Companion.EXTRA_RESULT_CODE, RESULT_OK)
                    }
                    startService(intent)
                    finish()
                }

            } catch (e: Exception) {
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                val intent = Intent(this, OverlayService::class.java).apply {
                    setAction(OverlayService.Companion.ACTION_SCREENSHOT_PERMISSION_GRANTED)
                    putExtra(OverlayService.Companion.EXTRA_RESULT_CODE, resultCode)
                    putExtra(OverlayService.Companion.EXTRA_RESULT_DATA, data)
                }
                startService(intent)
            } else {
                val intent = Intent(this, OverlayService::class.java).apply {
                    setAction(OverlayService.Companion.ACTION_SCREENSHOT_PERMISSION_DENIED)
                }
                startService(intent)
            }
        }

        finish()
    }
}