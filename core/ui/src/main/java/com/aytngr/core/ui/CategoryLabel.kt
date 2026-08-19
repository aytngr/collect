package com.aytngr.core.ui

import androidx.annotation.StringRes
import com.aytngr.domain.models.NoteCategory

@StringRes
fun NoteCategory.labelRes(): Int = when (this) {
    NoteCategory.GENERAL -> R.string.category_general
    NoteCategory.SHOPPING -> R.string.category_shopping
    NoteCategory.FINANCE -> R.string.category_finance
    NoteCategory.WORK -> R.string.category_work
    NoteCategory.SAVED -> R.string.category_saved
}
