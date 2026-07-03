package com.example.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized spacing scale based on a 4dp grid (Material 3 guidance).
 * Use these tokens instead of hardcoded dp values so spacing stays
 * consistent across every screen and component.
 */
object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Default horizontal/vertical inset from the screen edge. */
    val screen = 16.dp
}
