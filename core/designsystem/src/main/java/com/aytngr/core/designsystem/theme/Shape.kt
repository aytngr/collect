package com.aytngr.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small  = RoundedCornerShape(12.dp),  // chips, pills, input fields
    medium = RoundedCornerShape(16.dp),  // cards, list items, dialogs
    large  = RoundedCornerShape(24.dp),  // bottom sheets, expanded panels
)