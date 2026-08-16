package com.aytngr.core.common.base

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


val formatter = DateTimeFormatter.ofPattern("EEE · MMM d", Locale.getDefault())

fun Long.formatCreatedAt(): String {
    val zone = ZoneId.systemDefault()

    val dateTime = Instant.ofEpochMilli(this)
        .atZone(zone)
        .toLocalDateTime()

    val now = LocalDateTime.now(zone)
    val today = now.toLocalDate()
    val date = dateTime.toLocalDate()

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault())

    return when (date) {
        today -> "Today · ${dateTime.format(timeFormatter)}"

        today.minusDays(1) -> "Yesterday · ${dateTime.format(timeFormatter)}"

        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            "${dateTime.format(dateFormatter)} · ${dateTime.format(timeFormatter)}"
        }
    }
}
fun LocalDate.formatDate(): String =
    formatter.format(this)


fun Long.formatTimeAgo(): String {
    val now = Instant.now()
    val created = Instant.ofEpochMilli(this)

    val duration = Duration.between(created, now)

    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM")
                .withLocale(Locale.getDefault())
                .withZone(java.time.ZoneId.systemDefault())

            formatter.format(created)
        }
    }

}