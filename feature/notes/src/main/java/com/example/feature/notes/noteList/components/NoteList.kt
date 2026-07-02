package com.example.feature.notes.noteList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core.common.base.formatTimeAgo
import com.example.core.designsystem.theme.Spacing
import com.example.core.ui.GridNoteItem
import com.example.core.ui.NoteItem
import com.example.domain.models.Note
import com.example.feature.notes.noteList.enums.ViewMode

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
                    time = note.updatedAt.formatTimeAgo(),
                    category = note.category.name,
                    isPinned = note.isPinned,
                    reminder = note.reminderAt?.formatTimeAgo(),
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
                    time = note.updatedAt.formatTimeAgo(),
                    category = note.category.name,
                    isPinned = note.isPinned,
                    reminder = note.reminderAt?.formatTimeAgo(),
                    image = note.images?.firstOrNull(),
                    isSelected = note.id in selectedIds,
                    selectionMode = selectionMode,
                    onClick = { onItemClick(note.id) },
                    onLongClick = { onLongPress(note.id) },
                )
            }
        }
    }
}
