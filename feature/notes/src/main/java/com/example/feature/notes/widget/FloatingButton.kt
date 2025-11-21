package com.example.feature.notes.widget

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun FloatingButton(
    windowManager: WindowManager?,
    layoutParams: WindowManager.LayoutParams,
    view: android.view.View, onClick: () -> Unit, isRightSide: Boolean, saveWidgetLocation: suspend (x: Int, y: Int, isRightSide: Boolean) -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    var totalHorizontalDrag by remember { mutableFloatStateOf(0f) }
    var isRightSide by remember { mutableStateOf(isRightSide) }
    var isLongPressDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope ()

    Box(
        modifier = Modifier
            .clickable(onClick = {onClick()})
            .size(height = 56.dp, width = 30.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isLongPressDragging = true
                        totalHorizontalDrag = 0f

                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    50,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(50)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        totalHorizontalDrag += dragAmount.x

                        layoutParams.y = (layoutParams.y + dragAmount.y.toInt())

                        windowManager?.updateViewLayout(view, layoutParams)
                    },
                    onDragEnd = {
                        val switchThreshold = with(density) { 100.dp.toPx() }

                        val isCurrentlyOnRight = layoutParams.gravity and Gravity.END == Gravity.END

                        if (totalHorizontalDrag > switchThreshold && !isCurrentlyOnRight) {
                            layoutParams.gravity = Gravity.END
                            layoutParams.x = 0
                            isRightSide = true
                        } else if (totalHorizontalDrag < -switchThreshold && isCurrentlyOnRight) {
                            layoutParams.gravity = Gravity.START
                            layoutParams.x = 0
                            isRightSide = false
                        }

                        windowManager?.updateViewLayout(view, layoutParams)
                        isLongPressDragging = false
                        coroutineScope.launch {
                            saveWidgetLocation(layoutParams.x, layoutParams.y, isRightSide)
                        }
                    },
                    onDragCancel = {
                        isLongPressDragging = false
                    }
                )
            }
            .pointerInput(isLongPressDragging, isRightSide) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (!isLongPressDragging) {
                        if (isRightSide && dragAmount < -10) {
                            onClick()
                        } else if (!isRightSide && dragAmount > 10) {
                            onClick()
                        }
                    }
                }
            }

    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .align(if (isRightSide) Alignment.CenterEnd else Alignment.CenterStart)
                .background(
                    color = if (isLongPressDragging) Color.Green.copy(alpha = 0.3f) else Color.White.copy(
                        alpha = 0.3f
                    ),
                    shape = RoundedCornerShape(
                        topStart = if (isRightSide) 3.dp else 0.dp,
                        bottomStart = if (isRightSide) 3.dp else 0.dp,
                        topEnd = if (isRightSide) 0.dp else 3.dp,
                        bottomEnd = if (isRightSide) 0.dp else 3.dp
                    )
                )
        )
    }
}