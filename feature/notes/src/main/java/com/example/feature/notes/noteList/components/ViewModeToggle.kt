package com.example.feature.notes.noteList.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Spacing
import com.example.core.ui.R
import com.example.feature.notes.noteList.enums.ViewMode

@Composable
fun ViewModeToggle(
    selected: ViewMode,
    onSelect: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(AppShapes.medium)
            .background(AppTheme.colors.surfaceAlt),
//            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        ToggleCell(
            icon = R.drawable.list,
            selected = selected == ViewMode.LIST,
            onClick = { onSelect(ViewMode.LIST) },
        )
        ToggleCell(
            icon = R.drawable.grid,
            selected = selected == ViewMode.GRID,
            onClick = { onSelect(ViewMode.GRID) },
        )
    }
}

@Composable
private fun ToggleCell(
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (selected) Color.White else Color.Transparent, label = "bg")
    val tint by animateColorAsState(
        if (selected) AppTheme.colors.accent else AppTheme.colors.faint, label = "tint"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(Modifier.padding(Spacing.md), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}