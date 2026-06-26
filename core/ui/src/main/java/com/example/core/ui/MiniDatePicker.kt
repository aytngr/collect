package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MiniDatePicker(
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = LocalDate.now(),
) {
    // displayed month — initialized once so month navigation isn't reset when `selected` changes
    var month by remember { mutableStateOf(YearMonth.from(selected ?: LocalDate.now())) }

    Column(modifier = modifier.fillMaxWidth()) {
        // ── header: <  June 2026  > ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = AppTheme.colors.sub,
                )
            }
            Text(
                text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = AppTextStyle.NoteTitle,
                color = AppTheme.colors.ink,
            )
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = AppTheme.colors.sub,
                )
            }
        }

        // ── weekday labels (Monday-first) ──
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint,
                )
            }
        }

        // ── day grid ──
        val leading = month.atDay(1).dayOfWeek.value - 1            // Mon=1..Sun=7
        val cells = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val enabled = date != null && !date.isBefore(minDate)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .then(
                                if (date != null && date == selected)
                                    Modifier.background(AppTheme.colors.accent)
                                else Modifier
                            )
                            .clickable(enabled = enabled) { date?.let(onSelect) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            Text(
                                text = "${date.dayOfMonth}",
                                style = AppTextStyle.NoteBody,
                                color = when {
                                    date == selected -> Color.White
                                    !enabled -> AppTheme.colors.faint
                                    else -> AppTheme.colors.ink
                                },
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }   // pad short last week
            }
        }
    }
}
