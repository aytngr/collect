package com.aytngr.domain.overlay

interface OverlayController {
    fun isRunning(): Boolean
    fun start()
}