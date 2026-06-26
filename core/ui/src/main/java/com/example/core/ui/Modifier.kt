package com.example.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.math.roundToInt

fun Modifier.scaled(scale: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(maxHeight = Constraints.Infinity)   // ← only height unbounded
    )
    val w = (placeable.width * scale).roundToInt()
    val h = (placeable.height * scale).roundToInt()
    layout(w, h) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}
@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit) = clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() }
) {
    onClick()
}

@Composable
fun Modifier.noRippleCombinedClickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) = combinedClickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
    onClick = onClick,
    onLongClick = onLongClick
)