package com.aytngr.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.Spacing

@Composable
fun ReminderBox(
    isNext: Boolean,
    title: String,
    date: String,
    isDetail: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isNext) AppTheme.colors.accent else AppTheme.colors.line,
                shape = AppShapes.medium
            )
            .clip(AppShapes.medium)
            .background(if (isNext) AppTheme.colors.accent.copy(alpha = 0.04f) else Color.White)
            .noRippleClickable(onClick = {})
            .padding(vertical = Spacing.lg, horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isNext) AppTheme.colors.accentSoft else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isNext) Color.Transparent else AppTheme.colors.line,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bell),
                tint = if (isNext) AppTheme.colors.accent else AppTheme.colors.sub,
                contentDescription = "",
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.md)
        ) {
            if(isDetail){
                Text(text = title, style = AppTextStyle.Eyebrow, color = AppTheme.colors.accent)
                Text(text = date, style = AppTextStyle.NoteTitle)
            }else{
                Text(text = title, style = AppTextStyle.NoteTitle)
                Text(text = date, style = AppTextStyle.Metadata, color = AppTheme.colors.faint)
            }

        }
        if(isDetail){
            TextButton(onClick = onEditClick){ Text(text = stringResource(R.string.reminder_box_edit), style = AppTextStyle.Button, color = AppTheme.colors.accent)}
        }else{
            Icon(
                painter = painterResource(id = R.drawable.chevron),
                tint = AppTheme.colors.faint,
                contentDescription = "",
                modifier = Modifier.size(18.dp)
            )
        }

    }
}
