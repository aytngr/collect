package com.example.feature.notes.noteList

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.notes.noteList.components.CategoryChip
import com.example.feature.notes.noteList.components.NoteList
import com.example.feature.notes.noteList.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NoteListContract.Effect.ShowDeleteConfirmation -> {}
                is NoteListContract.Effect.ShowToast -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("All Notes") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { query ->
                viewModel.handleIntent(NoteListContract.Intent.SearchNote(query))
            },
            onClearClick = {
                viewModel.handleIntent(NoteListContract.Intent.ClearSearch)
            }
        )
        CategoryChip(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { category ->
                viewModel.handleIntent(NoteListContract.Intent.FilterByCategory(category))
            }
        )
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.notes.isEmpty()) {
                        "No notes yet. Create your first note!"
                    } else {
                        "No notes match your search."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            NoteList(
                notes = state.filteredNotes,
                onItemClick = { note -> onItemClick(note) },
                onCombineNote = { draggedNote, targetNote ->
                    viewModel.handleIntent(
                        NoteListContract.Intent.CombineNotes(
                            draggedNote, targetNote
                        )
                    )
                },
                handleExpandedNotes = { note ->
                    viewModel.handleIntent(
                        NoteListContract.Intent.HandleExpandedNotes(
                            note
                        )
                    )
                },
                onMoveNote = { fromNote, toNote ->
                    viewModel.handleIntent(
                        NoteListContract.Intent.ReorderNotes(
                            fromNote, toNote
                        )
                    )
                },
                onDragEnd = {
                    viewModel.handleIntent(NoteListContract.Intent.UpdateNotesWithOrder)
                },
                deleteNote = { note ->
                    viewModel.handleIntent(
                        (NoteListContract.Intent.DeleteNote(
                            note
                        ))
                    )
                }
            )
        }
    }
}
