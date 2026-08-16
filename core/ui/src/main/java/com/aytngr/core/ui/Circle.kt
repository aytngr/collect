package com.aytngr.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aytngr.core.designsystem.theme.AppTheme

@Composable
fun AppCircle(){
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.accent)
            .size(8.dp)
    )
}