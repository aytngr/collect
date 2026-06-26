package com.example.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSheet(
    currentReminderAt: Long?,
    onSet: (Long) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    forQuickNoteOverlay: Boolean = false
) {
    var day by remember {
        mutableStateOf<LocalDate?>(
            currentReminderAt?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        )
    }
    var time by remember {
        mutableStateOf<LocalTime?>(currentReminderAt?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        })
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box{
        if(forQuickNoteOverlay){
            ReminderSheetContent(
                currentReminderAt = currentReminderAt,
                day = day,
                time = time,
                onDay = { day = it },
                onTime = { time = it },
                onPickDate = { showDatePicker = true },
                onPickTime = { showTimePicker = true },
                onSet = onSet,
                onRemove = {
                    day = null
                    time = null
                    onRemove()
                },
                onDismiss = onDismiss,
                forQuickNoteOverlay = true,
                modifier = Modifier.clip(AppShapes.medium)
            )
        }else{
            ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppTheme.colors.bg) {
                ReminderSheetContent(
                    currentReminderAt = currentReminderAt,
                    day = day,
                    time = time,
                    onDay = { day = it },
                    onTime = { time = it },
                    onPickDate = { showDatePicker = true },
                    onPickTime = { showTimePicker = true },
                    onSet = onSet,
                    onRemove = onRemove,
                    onDismiss = onDismiss,
                )
            }
        }

        //inline pickers for quick note overlay

        if (forQuickNoteOverlay && showDatePicker) {
            var pendingDate by remember { mutableStateOf(day ?: LocalDate.now()) }
            InlinePickerCard(
                onCancel = { showDatePicker = false },
                onSelect = {
                    day = pendingDate
                    showDatePicker = false
                },
            ) {
                MiniDatePicker(
                    selected = pendingDate,
                    onSelect = { pendingDate = it },
                )
            }
        }

        if (forQuickNoteOverlay && showTimePicker) {
            val state = rememberTimePickerState(
                initialHour = time?.hour ?: LocalTime.now().hour,
                initialMinute = time?.minute ?: 0,
                is24Hour = true,
            )
            InlinePickerCard(
                modifier = Modifier.matchParentSize(),
                onCancel = { showTimePicker = false },
                onSelect = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                },
            ) {
                TimeInput(state = state)
            }
        }
    }


    if (!forQuickNoteOverlay && showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (day ?: LocalDate.now()).toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis >= LocalDate.now().toUtcMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { day = it.toLocalDateUtc() }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (!forQuickNoteOverlay && showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time?.hour ?: LocalTime.now().hour,
            initialMinute = time?.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) },
        )
    }
}


private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()