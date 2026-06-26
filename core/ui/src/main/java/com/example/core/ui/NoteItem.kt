package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteItem(
    title: String,
    content: String,
    time: String,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    category: String,
    isPinned: Boolean = false,
    reminder: String? = null,
    metadata: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {

    Row(
        modifier = Modifier
            .noRippleCombinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(shape = AppShapes.medium, color = if (selectionMode && isSelected) AppTheme.colors.accentSoft else Color.Transparent)

    ) {
        if (selectionMode) {
            Icon(
                painter = painterResource(if (isSelected) R.drawable.selected else R.drawable.unselected),
                contentDescription = "",
                tint = AppTheme.colors.accent,
                modifier = Modifier.padding(top = 12.dp, start = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPinned) {
                    Icon(
                        painter = painterResource(id = R.drawable.pin),
                        contentDescription = "Pinned",
                        Modifier.size(18.dp)
                    )
                    HorizontalSpacer(8.dp)
                }
                Text(text = title.takeIf { it.isNotEmpty() } ?: "Untitled",
                    style = AppTextStyle.NoteTitle,
                    modifier = Modifier.weight(1f))
                Text(
                    text = time, style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint
                )
            }

            if (content.isNotEmpty()) Text(
                text = content,
                style = AppTextStyle.BodySnippet,
                color = AppTheme.colors.sub,
                maxLines = 1,
            )

            VerticalSpacer(8.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = category, style = AppTextStyle.Eyebrow, color = AppTheme.colors.faint)
                HorizontalSpacer(16.dp)
                when {
                    reminder != null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.bell),
                                contentDescription = "",
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(12.dp)
                            )
                            HorizontalSpacer(4.dp)
                            Text(
                                text = reminder,
                                color = AppTheme.colors.accent,
                                style = AppTextStyle.VoiceReminderPill
                            )
                        }
                    }

                    metadata != null -> {
                        Text(
                            text = metadata,
                            color = AppTheme.colors.accent,
                            style = AppTextStyle.Metadata
                        )

                    }
                }
            }
            VerticalSpacer(8.dp)
            HorizontalDivider(color = AppTheme.colors.line)
        }
    }
}

@Composable
fun GridNoteItem(
    title: String,
    content: String,
    time: String,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    category: String,
    isPinned: Boolean = false,
    reminder: String? = null,
    image: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clip(AppShapes.large)
            .border(
                shape = AppShapes.large,
                width = 1.dp,
                color = if (selectionMode && isSelected) AppTheme.colors.accent else AppTheme.colors.line
            )
            .background(color = AppTheme.colors.surface)
            .noRippleCombinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = category, style = AppTextStyle.Eyebrow, color = AppTheme.colors.faint)
            WeightSpacer()
            if (isPinned) {
                Icon(
                    painter = painterResource(id = R.drawable.pin),
                    contentDescription = "Pinned",
                    Modifier.size(16.dp)
                )
            }
            if (selectionMode) {
                Icon(
                    painter = painterResource(if (isSelected) R.drawable.selected else R.drawable.unselected),
                    contentDescription = "",
                    tint = AppTheme.colors.accent,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp)
                )
            }
        }
        VerticalSpacer(8.dp)
        Text(text = title.takeIf { it.isNotEmpty() } ?: "Untitled",
            style = AppTextStyle.NoteTitle,
            maxLines = 1)
        VerticalSpacer(8.dp)
        image?.let {
            AsyncImage(
                model = File(image),
                contentDescription = "Screenshot",
                modifier = Modifier
                    .size(width = 100.dp, height = 80.dp)
                    .clip(AppShapes.small),
                contentScale = ContentScale.Crop,
                onError = {
                    android.util.Log.e(
                        "noteimg",
                        "load failed: $image",
                        it.result.throwable
                    )
                },
            )
        }
        VerticalSpacer(8.dp)
        if (content.isNotEmpty()) {
            Text(
                text = content,
                style = AppTextStyle.BodySnippet,
                color = AppTheme.colors.sub,
                maxLines = 4
            )
            VerticalSpacer(8.dp)
        }

        Row {
            WeightSpacer()
            Text(
                text = time, style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun NotePreview() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            content = "Hello",
            time = "1h",
            category = "IDEAS",
//        reminder = "",
//        metadata= "",
            onClick = {},
            onLongClick = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun NoteGridPreview() {
    TaskFlowTheme {
        GridNoteItem(
            title = "Hello",
            content = "How are you",
            time = "1h",
            image = "kjsndjkndja",
            category = "IDEAS",
//        reminder = "",
//        metadata= "",
            onClick = {},
            onLongClick = {},
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun NoteGridPreview2() {
    TaskFlowTheme {
        GridNoteItem(
            title = "Hello",
            content = "How are you",
            time = "1h",
            image = "kjsndjkndja",
            isSelected = true,
            selectionMode = true,
            category = "IDEAS",
//        reminder = "",
//        metadata= "",
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotePreview1() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            content = "Hello",
            time = "1h",
            category = "IDEAS",
//            recording = "0:10",
            reminder = "Thu 9:00",
//        metadata= "",
            onClick = {},
            onLongClick = {},
            selectionMode = true,
            isSelected = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotePreview2() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            content = "Hello",
            time = "1h",
            category = "IDEAS",
//            recording = "0:10",
//        reminder = "",
            metadata = "6 items",
            onClick = {},
            onLongClick = {}
        )
    }
}