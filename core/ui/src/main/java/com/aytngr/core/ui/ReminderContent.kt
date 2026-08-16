package com.aytngr.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.CollectTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderSheetContent(
    currentReminderAt: Long?,
    day: LocalDate?,
    time: LocalTime?,
    onDay: (LocalDate) -> Unit,
    onTime: (LocalTime?) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onSet: (Long) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    forQuickNoteOverlay: Boolean = false
) {
    val result = if (day != null && time != null) combine(day, time) else null

    if (forQuickNoteOverlay) {
        LaunchedEffect(result) {
            if (result != null) onSet(result)
        }
        LaunchedEffect(day) {
            if(day != null && time != null && combine(
                day,
                time
            ) < System.currentTimeMillis())
                onTime(null)
        }
    }

    Column(
        modifier
            .background(color = if (forQuickNoteOverlay) AppTheme.colors.surfaceAlt else AppTheme.colors.bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.bell),
                contentDescription = "",
                tint = AppTheme.colors.accent
            )
            HorizontalSpacer(8.dp)
            Text(stringResource(R.string.reminder_title), style = AppTextStyle.NoteTitle)
            WeightSpacer()
            if (forQuickNoteOverlay) TextButton(onClick = {
                onRemove() }) {
                Text(
                    text = stringResource(R.string.reminder_clear),
                    style = AppTextStyle.Button,
                    color = AppTheme.colors.sub
                )
            }
        }

        VerticalSpacer(12.dp)

        Text(stringResource(R.string.reminder_when_label), style = AppTextStyle.Eyebrow, color = AppTheme.colors.faint)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReminderChip(
                stringResource(R.string.reminder_day_today),
                day == DayOption.TODAY.toDate()
            ) { onDay(DayOption.TODAY.toDate()) }
            ReminderChip(
                stringResource(R.string.reminder_day_tomorrow),
                day == DayOption.TOMORROW.toDate()
            ) { onDay(DayOption.TOMORROW.toDate()) }
            ReminderChip(
                stringResource(R.string.reminder_day_weekend),
                day == DayOption.WEEKEND.toDate()
            ) { onDay(DayOption.WEEKEND.toDate()) }
            ReminderChip(
                stringResource(R.string.reminder_day_next_week),
                day == DayOption.NEXT_WEEK.toDate()
            ) { onDay(DayOption.NEXT_WEEK.toDate()) }
            ReminderChip(
                label = day?.takeIf { it.isCustom() }?.let(::dateLabel) ?: stringResource(R.string.reminder_pick_date),
                selected = day?.isCustom() == true,
            ) { onPickDate() }
        }

        VerticalSpacer(12.dp)

        Text(stringResource(R.string.reminder_time_label), style = AppTextStyle.Eyebrow, color = AppTheme.colors.faint)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeOption.entries.forEach { opt ->
                ReminderChip(
                    label = stringResource(opt.labelRes()),
                    selected = time == opt.toTime() && (day != null && combine(
                        day,
                        opt.toTime()
                    ) > System.currentTimeMillis()),
                    enabled = !(day != null && combine(
                        day,
                        opt.toTime()
                    ) <= System.currentTimeMillis()),
                ) { onTime(opt.toTime()) }
            }
            ReminderChip(
                label = time?.takeIf { it.isCustom() }?.let(::timeLabel) ?: stringResource(R.string.reminder_pick_time),
                selected = time?.isCustom() == true,
            ) { onPickTime() }
        }

        VerticalSpacer(16.dp)

        result?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = AppTheme.colors.accent,
                        shape = AppShapes.small
                    )
                    .clip(AppShapes.medium)
                    .background(color = AppTheme.colors.accentSoft)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(formatReminder(it), style = AppTextStyle.ReminderTitle)

            }
            VerticalSpacer(12.dp)
        }

        if(!forQuickNoteOverlay){
            Row(Modifier.fillMaxWidth()) {
                if (currentReminderAt != null) {
                    TextButton(onClick = onRemove) {
                        Text(
                            stringResource(R.string.reminder_remove),
                            style = AppTextStyle.Button,
                            color = AppTheme.colors.sub
                        )
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.reminder_cancel),
                            style = AppTextStyle.Button,
                            color = AppTheme.colors.sub
                        )
                    }
                }
                HorizontalSpacer(16.dp)
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.medium),
                    onClick = { result?.let(onSet) },
                    enabled = result != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.accent
                    )
                ) { Text(stringResource(R.string.reminder_set), style = AppTextStyle.Button) }
            }
        }

        VerticalSpacer(8.dp)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderChip(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onSelect: () -> Unit,
) {
    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = onSelect,
        label = { Text(label, style = AppTextStyle.Chip) },
        shape = AppShapes.small,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = AppTheme.colors.sub,
            selectedContainerColor = AppTheme.colors.accent,
            selectedLabelColor = Color.White,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = AppTheme.colors.line,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

private fun LocalDate.isCustom(): Boolean = DayOption.entries.none { it.toDate() == this }
private fun LocalTime.isCustom(): Boolean = TimeOption.entries.none { it.toTime() == this }

private fun dateLabel(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))

private fun timeLabel(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

private fun formatReminder(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a", Locale.getDefault()))


@Preview(showBackground = true)
@Composable
private fun ReminderSheetContentPreview() {
    CollectTheme {
        ReminderSheetContent(
            currentReminderAt = null,
            day = DayOption.TOMORROW.toDate(),
            time = TimeOption.MORNING.toTime(),
            onDay = {},
            onTime = {},
            onPickDate = {},
            onPickTime = {},
            onSet = {},
            onRemove = {},
            onDismiss = {},
            forQuickNoteOverlay = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderSheetContentEditingPreview() {
    CollectTheme {
        ReminderSheetContent(
            currentReminderAt = System.currentTimeMillis(),
            day = LocalDate.now().plusDays(3),
            time = LocalTime.of(7, 30),
            onDay = {},
            onTime = {},
            onPickDate = {},
            onPickTime = {},
            onSet = {},
            onRemove = {},
            onDismiss = {},
        )
    }
}
