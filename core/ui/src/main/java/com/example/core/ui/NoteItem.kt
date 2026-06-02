package com.example.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteItem(
    title: String,
    time: String,
    category: String,
    isFav: Boolean = false,
    recording: String? = null,
    reminder: String? = null,
    metadata: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isFav) {
                Icon(
                    painter = painterResource(id = R.drawable.pin),
                    contentDescription = "Pinned",
                    Modifier.size(18.dp)
                )
                HorizontalSpacer(8.dp)
            }
            Text(text = title, style = AppTextStyle.NoteTitle, modifier = Modifier.weight(1f))
            Text(
                text = time, style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )
        }
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

                recording != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.mic),
                            contentDescription = "",
                            modifier = Modifier.size(12.dp),
                            tint = AppTheme.colors.accent
                        )
                        HorizontalSpacer(4.dp)
                        Text(
                            text = recording,
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
    }
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .clickable(onClick = { onClick() }),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.Top
//        ) {
//            Text(
//                text = note.content,
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(PaddingValues(16.dp)),
//                maxLines = 3,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(PaddingValues(horizontal = 16.dp)),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            AssistChip(
//                onClick = {},
//                label = {
//                    Text(text = note.category.name.lowercase().replaceFirstChar { it.uppercase() })
//                }
//            )
//            Text(
//                text = note.language.displayName,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = formatDate(note.createdAt),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//        if (note.extractedData.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(8.dp))
//            note.extractedData.forEach { (key, value) ->
//                Text(
//                    text = "$key: $value",
//                    modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
private fun NotePreview() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            time = "1h",
            category = "IDEAS",
            recording = "0:10",
//        reminder = "",
//        metadata= "",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotePreview1() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            time = "1h",
            category = "IDEAS",
//            recording = "0:10",
            reminder = "Thu 9:00",
//        metadata= "",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotePreview2() {
    TaskFlowTheme {
        NoteItem(
            title = "Hello",
            time = "1h",
            category = "IDEAS",
//            recording = "0:10",
//        reminder = "",
            metadata = "6 items",
            onClick = {},
        )
    }
}