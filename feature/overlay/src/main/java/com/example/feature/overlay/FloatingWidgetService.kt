package com.example.feature.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.domain.models.Language
import com.example.domain.models.SaveStatus
import com.example.domain.models.onError
import com.example.domain.models.onSuccess
import com.example.domain.usecase.CreateNoteUseCase
import com.example.domain.usecase.GetWidgetLocationUseCase
import com.example.domain.usecase.SaveWidgetLocationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private var widgetView: View? = null
    private var quickNoteView: View? = null
    private var popupParams: WindowManager.LayoutParams? = null
    private var screenshotAnimationView: ComposeView? = null
    var isQuickNoteAdded = false

    private var currentText = ""
    private var currentScreenshots: MutableList<Bitmap>? = mutableListOf<Bitmap>()

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    @Inject
    lateinit var saveWidgetLocationUseCase: SaveWidgetLocationUseCase

    @Inject
    lateinit var getWidgetLocationUseCase: GetWidgetLocationUseCase

    @Inject
    lateinit var createNoteUseCase: CreateNoteUseCase
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var screenshotManager: ScreenshotManager? = null

    companion object {
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_SCREENSHOT_PERMISSION_GRANTED = "action_screenshot_permission_granted"
        const val ACTION_SCREENSHOT_PERMISSION_DENIED = "action_screenshot_permission_denied"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }


    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    val screenshotValue: MutableLiveData<Bitmap?> = MutableLiveData<Bitmap?>(null)

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        screenshotManager = ScreenshotManager(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notification = createNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        createOverlayWidget()
    }

    private fun createOverlayWidget() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        lifecycleScope.launch {
            val (xPos, yPos, isRightSide) = getWidgetLocationUseCase()
            params.x = 0
            params.y = yPos ?: 0
            params.gravity = if (isRightSide == true) Gravity.END else Gravity.START

            withContext(Dispatchers.Main) {
                addOverlayView(params, isRightSide)
            }
        }
    }

    private fun addOverlayView(params: WindowManager.LayoutParams, isRightSide: Boolean?) {
        widgetView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                FloatingButton(
                    onClick = {
                        addQuickNotePopup()
                    },
                    windowManager = windowManager,
                    layoutParams = params,
                    view = this,
                    isRightSide = isRightSide ?: true,
                    saveWidgetLocation = ::saveWidgetLocation
                )
            }
        }

        windowManager.addView(widgetView, params)
    }

    private suspend fun saveWidgetLocation(x: Int, y: Int, isRightSide: Boolean) {
        saveWidgetLocationUseCase(x, y, isRightSide)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Note overlay button service"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Quick Note")
            .setContentText("Overlay button is active")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }


    private fun addQuickNotePopup() {
        // Check if popup already created
        if (!isQuickNoteAdded) {
            // Create QuickNotesOverlay
            val vibrator =
                this.getSystemService(VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        30,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }

            quickNoteView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeViewModelStoreOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)

                setContent {
                    QuickNotesOverlay(
                        text = currentText,
                        onSave = ::saveNoteToDatabase,
                        onClose = { removeQuickNotePopup() },
                        onHide = { hideQuickNotePopup() },
                        onScreenshot = { text, screenshots ->
                            currentScreenshots = screenshots?.toMutableList()
                            currentText = text
                            ScreenshotActivity.start(
                                this@OverlayService,
                                screenshotManager?.getMediaProjection()
                            )
                        },
                        screenshots = currentScreenshots
                    )
                }
            }

            popupParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                dimAmount = 0.5f
            }

            windowManager.addView(quickNoteView, popupParams)
            isQuickNoteAdded = true
        } else {
            // Reshow hidden QuickNotesOverlay
            showQuickNotePopup()
        }
    }

    private fun captureAndShowNoteWindow() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        lifecycleScope.launch {
            try {
                hideQuickNotePopup()
                widgetView?.visibility = INVISIBLE
                showScreenshotAnimation()
                delay(250)
                hideScreenshotAnimation()
                val screenshot = screenshotManager?.captureScreenshot()
                withContext(Dispatchers.Main) {
                    if (screenshot != null) {
                        // Recompose QuickNotesOverlay with new parameters
                        (quickNoteView as ComposeView).setContent {
                            QuickNotesOverlay(
                                text = currentText,
                                screenshots = (currentScreenshots.orEmpty() + screenshot),
                                onSave = ::saveNoteToDatabase,
                                onHide = { hideQuickNotePopup() },
                                onClose = { removeQuickNotePopup() },
                                onScreenshot = { text, screenshots ->
                                    currentText = text
                                    currentScreenshots = screenshots?.toMutableList()
                                    ScreenshotActivity.start(
                                        this@OverlayService,
                                        screenshotManager?.getMediaProjection()
                                    )
                                }
                            )
                        }
                    }
                }

                widgetView?.visibility = VISIBLE
                showQuickNotePopup()
            } finally {
                switchToSpecialUseType()
            }
        }
    }

    private fun showScreenshotAnimation() {
        if (screenshotAnimationView != null) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        screenshotAnimationView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                MaterialTheme {
                    ScreenshotFlashAnimation()
                }
            }
        }

        windowManager?.addView(screenshotAnimationView, params)
    }

    private fun hideScreenshotAnimation() {
        screenshotAnimationView?.let {
            windowManager?.removeView(it)
            screenshotAnimationView = null
        }
    }

    private suspend fun saveNoteToDatabase(
        noteText: String,
        images: List<Bitmap?>?,
        onSaveStatus: (SaveStatus) -> Unit
    ) {
        createNoteUseCase(
            title = null,
            content = noteText,
            images = images?.map { it?.saveToStorage(this) },
            language = Language.AZERBAIJANI
        )
            .onSuccess {
                onSaveStatus(SaveStatus.SUCCESS)
            }
            .onError {
                onSaveStatus(SaveStatus.ERROR)
            }
    }

    private fun removeQuickNotePopup() {
        currentScreenshots = mutableListOf<Bitmap>()
        windowManager.removeView(quickNoteView)
        isQuickNoteAdded = false
    }

    private fun hideQuickNotePopup() {
        quickNoteView?.apply {
            alpha = 0f
            visibility = INVISIBLE
        }
    }

    private fun showQuickNotePopup() {
        quickNoteView?.apply {
            alpha = 1f
            visibility = VISIBLE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        widgetView?.let { windowManager.removeView(it) }
        store.clear()
        coroutineScope.cancel()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        when (intent?.action) {
            ACTION_SCREENSHOT_PERMISSION_GRANTED -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                switchToMediaProjectionType()

                data?.let {
                    screenshotManager?.setupMediaProjection(resultCode, data)
                    captureAndShowNoteWindow()
                } ?: run {
                    captureAndShowNoteWindow()
                }
            }

            ACTION_SCREENSHOT_PERMISSION_DENIED -> {

            }
        }

        return START_STICKY
    }

    private fun switchToMediaProjectionType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val notification = createNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun switchToSpecialUseType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}