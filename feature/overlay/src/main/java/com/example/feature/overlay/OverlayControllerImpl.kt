package com.example.feature.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.overlay.OverlayController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OverlayControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlayController {
    override fun isRunning() = OverlayService.isRunning
    override fun start() {
        val i = Intent(context, OverlayService::class.java)
        context.startForegroundService(i)
    }
}