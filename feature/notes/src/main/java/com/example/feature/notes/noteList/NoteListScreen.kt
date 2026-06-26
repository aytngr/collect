package com.example.feature.notes.noteList

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Sub
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.core.ui.R
import com.example.core.ui.VerticalSpacer
import com.example.core.ui.WeightSpacer
import com.example.core.ui.noRippleClickable
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.feature.notes.noteList.components.CategoryChip
import com.example.feature.notes.noteList.components.NotesView
import com.example.feature.notes.noteList.components.SearchBar
import com.example.feature.notes.noteList.components.ViewModeToggle
import com.example.feature.notes.noteList.enums.ViewMode

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

    NoteListContent(
        state = state,
        onIntent = viewModel::handleIntent,
        onBackClick = onBackClick,
        onItemClick = onItemClick
    )
}

@Composable
private fun NoteListContent(
    state: NoteListContract.State,
    onIntent: (NoteListContract.Intent) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    var showMovePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colors.bg)
            .padding(16.dp)
    ) {
        if (state.selectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "Cancel",
                    style = AppTextStyle.Button,
                    color = AppTheme.colors.accent,
                    modifier = Modifier.noRippleClickable(onClick = {
                        onIntent(
                            NoteListContract.Intent.ClearSelection
                        )
                    })
                )
                Text("${state.selectedIds.size} selected", style = AppTextStyle.SheetTitle)
                Text(
                    "Select all",
                    style = AppTextStyle.Button,
                    color = AppTheme.colors.sub,
                    modifier = Modifier.noRippleClickable(onClick = {
                        onIntent(
                            NoteListContract.Intent.SelectAll
                        )
                    })
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(50.dp)) {
                IconButton(
                    onClick = { onBackClick() }
                ) {
                    Icon(painter = painterResource(R.drawable.chevron), contentDescription = "")
                }
                Text("All notes", style = AppTextStyle.DetailHeadline)
                WeightSpacer()
                Text(
                    text = state.notes.size.toString(),
                    style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint
                )
            }
        }

        VerticalSpacer(16.dp)
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { query ->
                onIntent(NoteListContract.Intent.SearchNote(query))
            },
            onClearClick = {
                onIntent(NoteListContract.Intent.ClearSearch)
            }
        )
        VerticalSpacer(8.dp)
        CategoryChip(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { category ->
                onIntent(NoteListContract.Intent.FilterByCategory(category))
            }
        )

        Row {
            WeightSpacer()
            ViewModeToggle(
                selected = viewMode,
                onSelect = { viewMode = it }
            )
        }

        VerticalSpacer(8.dp)


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
                    style = AppTextStyle.BodySnippet,
                    color = Sub
                )
            }
        } else {
            NotesView(
                modifier = Modifier.weight(1f),
                notes = state.filteredNotes,
                viewMode = viewMode,
                selectionMode = state.selectionMode,
                selectedIds = state.selectedIds,
                onItemClick = { id ->
                    if (state.selectionMode) onIntent(NoteListContract.Intent.ToggleSelect(id))
                    else onItemClick(id)
                },
                onLongPress = { id -> onIntent(NoteListContract.Intent.LongPressNote(id)) },
            )
        }

        if (state.selectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionAction(
                    icon = R.drawable.pin,
                    label = "Pin",
                    onClick = { onIntent(NoteListContract.Intent.PinSelected) },
                )
                SelectionAction(
                    icon = R.drawable.move,
                    label = "Move",
                    onClick = { showMovePicker = true },
                )
                SelectionAction(
                    icon = R.drawable.merge,
                    label = "Combine",
                    onClick = { onIntent(NoteListContract.Intent.CombineSelected) },
                )
                SelectionAction(
                    icon = R.drawable.delete,
                    label = "Delete",
                    onClick = { onIntent(NoteListContract.Intent.DeleteSelected) },
                )
            }
        }
    }

    if (showMovePicker) {
        MoveCategoryPicker(
            onDismiss = { showMovePicker = false },
            onPick = { category ->
                onIntent(NoteListContract.Intent.MoveSelected(category))
                showMovePicker = false
            },
        )
    }
}

@Composable
private fun MoveCategoryPicker(
    onDismiss: () -> Unit,
    onPick: (NoteCategory) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to", style = AppTextStyle.SheetTitle) },
        text = {
            Column {
                NoteCategory.entries.forEach { category ->
                    Text(
                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = AppTextStyle.NoteBody,
                        color = AppTheme.colors.sub,
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable() { onPick(category) }
                            .padding(vertical = 14.dp),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SelectionAction(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = AppTheme.colors.sub,
        )
        VerticalSpacer(4.dp)
        Text(label, style = AppTextStyle.Metadata, color = AppTheme.colors.sub)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    TaskFlowTheme {
        NoteListContent(
            state = NoteListContract.State(
                notes = listOf(Note(), Note())
            ),
            onIntent = {},
            onBackClick = {}
        ) { }
    }
}
