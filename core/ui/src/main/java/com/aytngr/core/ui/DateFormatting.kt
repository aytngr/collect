package com.aytngr.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val timeOnly: DateTimeFormatter
    get() = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())

private val dayAndMonth: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

/** "Today · 14:30", "Yesterday · 09:15", "Mar 4 · 18:00" */
@Composable
fun createdAtLabel(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
    val today = LocalDateTime.now(zone).toLocalDate()
    val date = dateTime.toLocalDate()

    val day = when (date) {
        today -> stringResource(R.string.time_today)
        today.minusDays(1) -> stringResource(R.string.time_yesterday)
        else -> dateTime.format(dayAndMonth)
    }
    return stringResource(R.string.time_day_at_time, day, dateTime.format(timeOnly))
}

/** Compact elapsed time for list rows: "Just now", "5m", "3h", "Yesterday", "4d". */
@Composable
fun timeAgoLabel(millis: Long): String {
    val elapsed = Duration.between(Instant.ofEpochMilli(millis), Instant.now())
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()

    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_minutes_short, minutes.toInt())
        hours < 24 -> stringResource(R.string.time_hours_short, hours.toInt())
        days == 1L -> stringResource(R.string.time_yesterday)
        days < 7 -> stringResource(R.string.time_days_short, days.toInt())
        else -> Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dayAndMonth)
    }
}

/** "Edited just now", "Edited 5m ago", "Edited on Mar 4". */
@Composable
fun editedLabel(millis: Long): String {
    val elapsed = Duration.between(Instant.ofEpochMilli(millis), Instant.now())
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()

    return when {
        minutes < 1 -> stringResource(R.string.time_edited_just_now)
        minutes < 60 -> stringResource(R.string.time_edited_minutes, minutes.toInt())
        hours < 24 -> stringResource(R.string.time_edited_hours, hours.toInt())
        days == 1L -> stringResource(R.string.time_edited_yesterday)
        days < 7 -> stringResource(R.string.time_edited_days, days.toInt())
        else -> stringResource(
            R.string.time_edited_on,
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dayAndMonth)
        )
    }
}
