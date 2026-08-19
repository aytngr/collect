package com.aytngr.feature.notes.noteList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aytngr.core.designsystem.theme.Spacing
import com.aytngr.core.ui.GridNoteItem
import com.aytngr.core.ui.NoteItem
import com.aytngr.core.ui.createdAtLabel
import com.aytngr.core.ui.timeAgoLabel
import com.aytngr.domain.models.Note
import com.aytngr.feature.notes.noteList.enums.ViewMode
import androidx.compose.ui.res.stringResource
import com.aytngr.core.ui.labelRes

@Composable
fun NotesView(
    notes: List<Note>,
    viewMode: ViewMode,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onItemClick: (Long) -> Unit,
    onLongPress: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (viewMode) {
        ViewMode.LIST -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(items = notes, key = { it.id }) { note ->
                NoteItem(
                    title = note.title,
                    content = note.content,
                    time = timeAgoLabel(note.updatedAt),
                    category = stringResource(note.category.labelRes()),
                    isPinned = note.isPinned,
                    reminder = note.reminderAt?.let { createdAtLabel(it) },
                    metadata = null,
                    isSelected = note.id in selectedIds,
                    selectionMode = selectionMode,
                    onClick = { onItemClick(note.id) },
                    onLongClick = { onLongPress(note.id) },
                )
            }
        }

        ViewMode.GRID -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            gridItems(items = notes, key = { it.id }) { note ->
                GridNoteItem(
                    title = note.title,
                    content = note.content,
                    time = timeAgoLabel(note.updatedAt),
                    category = stringResource(note.category.labelRes()),
                    isPinned = note.isPinned,
                    reminder = note.reminderAt?.let { createdAtLabel(it) },
                    image = note.images.firstOrNull(),
                    isSelected = note.id in selectedIds,
                    selectionMode = selectionMode,
                    onClick = { onItemClick(note.id) },
                    onLongClick = { onLongPress(note.id) },
                )
            }
        }
    }
}
