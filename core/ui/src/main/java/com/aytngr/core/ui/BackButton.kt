package com.aytngr.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aytngr.core.designsystem.theme.AppTheme

/**
 * A leading navigation (back) icon backed by [IconButton], so it gets the
 * standard 48dp touch target and circular ripple for free.
 */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.ink,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
    }

}
