package com.aytngr.core.common.base

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val headerDate = DateTimeFormatter.ofPattern("EEE · MMM d", Locale.getDefault())

/**
 * Pattern-only, so it needs no string resources and stays usable outside the UI layer.
 * Anything with words in it lives in core:ui/DateFormatting.kt as a @Composable.
 */
fun LocalDate.formatDate(): String = headerDate.format(this)
