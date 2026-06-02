package com.example.feature.notes.noteList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.ui.NoteItem
import com.example.domain.models.Note
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun NoteList(
    notes: List<Note>,
    onCombineNote: (Note, Note) -> Unit,
    onMoveNote: (fromId: Long, toId: Long) -> Unit,
    onItemClick: (Long) -> Unit,
    handleExpandedNotes: (Note) -> Unit,
    onDragEnd: () -> Unit,
    deleteNote: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the list
        onMoveNote.invoke(from.key as Long, to.key as Long)
        onDragEnd.invoke()
    }


    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = notes,
                key = { it.id }
            ) { note ->

                val onClick = remember(note.id) { { onItemClick(note.id) } }
                val onDelete = remember(note) { { showDeleteDialog = true; noteToDelete = note } }
                val onExpand = remember(note) { { handleExpandedNotes(note) } }

                ReorderableItem(reorderableLazyListState, key = note.id) { isDragging ->
                    // Item content

//                    SwipeableItemWithAction(
//                        isRevealed = note.isRevealed,
//                        actions = {
//                            ActionIcon(
//                                onClick = onDelete,
//                                backgroundColor = Color.White,
//                                icon = Icons.Default.Delete,
//                                tint = Color.Black,
//                                modifier = Modifier.fillMaxHeight()
//                            )
//                        },
//                        onDragStart = onExpand,
//                        modifier = Modifier.draggableHandle(),
//                    ) {
//                    NoteItem(
//                        note = note,
//                        onClick = onClick,
//                        modifier = Modifier.longPressDraggableHandle(
//                            onDragStopped = { onDragEnd() }
//                        )
//                    )
//                    }
                }


            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            noteToDelete?.let { deleteNote(it) }
                            showDeleteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}