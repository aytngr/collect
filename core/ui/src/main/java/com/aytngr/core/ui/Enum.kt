package com.aytngr.core.ui

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class DayOption { TODAY, TOMORROW, WEEKEND, NEXT_WEEK }
enum class TimeOption { MORNING, NOON, EVENING, NIGHT }

fun DayOption.toDate(today: LocalDate = LocalDate.now()): LocalDate = when (this){
    DayOption.TODAY -> today
    DayOption.TOMORROW -> today.plusDays(1)
    DayOption.WEEKEND -> today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
    DayOption.NEXT_WEEK -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
}

fun TimeOption.toTime(): LocalTime = when (this) {
    TimeOption.MORNING -> LocalTime.of(9, 0)
    TimeOption.NOON    -> LocalTime.of(12, 0)
    TimeOption.EVENING -> LocalTime.of(18, 0)
    TimeOption.NIGHT   -> LocalTime.of(21, 0)
}

fun TimeOption.labelRes(): Int = when (this) {
    TimeOption.MORNING -> R.string.reminder_time_morning
    TimeOption.NOON -> R.string.reminder_time_noon
    TimeOption.EVENING -> R.string.reminder_time_evening
    TimeOption.NIGHT -> R.string.reminder_time_night
}

fun combine(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long =
    date.atTime(time).atZone(zone).toInstant().toEpochMilli()
