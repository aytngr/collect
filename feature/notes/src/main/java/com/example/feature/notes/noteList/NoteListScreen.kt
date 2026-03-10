package com.example.feature.notes.noteList

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.toInt
import androidx.compose.runtime.toString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
        CategoryFilter(
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
            NotesList(
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding((16.dp)),
        placeholder = { Text("Search notes...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search"
                    )
                }
            }
        } else null,
        singleLine = true,
    )
}

@Composable
private fun CategoryFilter(
    selectedCategory: NoteCategory?,
    onCategorySelected: (NoteCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )
        }
        items(NoteCategory.entries.toTypedArray()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { (Text(category.name.lowercase().replaceFirstChar { it.uppercase() })) }
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick() }),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(PaddingValues(16.dp)),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 16.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = {},
                label = {
                    Text(text = note.category.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            )
            Text(
                text = note.language.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDate(note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (note.extractedData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            note.extractedData.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SwipeableItemWithAction(
    isRevealed: Boolean,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit = {},
    onExpanded: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    content: @Composable () -> Unit,
) {

    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = isRevealed, contextMenuWidth) {
        if (isRevealed) {
            offset.animateTo(-contextMenuWidth, animationSpec = tween(100))
        } else {
            offset.animateTo(0f, animationSpec = tween(100))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .onSizeChanged {
                    contextMenuWidth = it.width.toFloat()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(offset.value.roundToInt(), 0)
                }
                .clip(MaterialTheme.shapes.medium)
                .pointerInput(contextMenuWidth) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset =
                                    minOf(offset.value + dragAmount / 2, 0f)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            when {
                                offset.value <= -contextMenuWidth / 2f -> {
                                    scope.launch {
                                        offset.animateTo(-contextMenuWidth)
                                        onExpanded()
                                    }
                                }

                                else -> {
                                    scope.launch {
                                        offset.animateTo(0f)
                                        onCollapsed()
                                    }
                                }
                            }
                        },
                        onDragStart = { onDragStart() }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun ActionIcon(
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: ImageVector,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
fun NotesList(
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

    val listState = rememberLazyListState()

    var draggingItem by remember { mutableStateOf<LazyListItemInfo?>(null) }
    var draggingItemKey by remember { mutableStateOf<Long?>(null) }
    var delta by remember { mutableFloatStateOf(0f) }
    var targetKey by remember { mutableStateOf<Long?>(null) }

    val scrollChannel = remember { Channel<Float>(Channel.CONFLATED) }

    LaunchedEffect(listState) {
        while (true) {
            val diff = scrollChannel.receive()
            if (diff == 0f) continue
            val consumed = listState.scrollBy(diff)
            delta += consumed
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .pointerInput(listState) {
                    coroutineScope {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { item ->
                                        offset.y.toInt() in item.offset..(item.offset + item.size)
                                    }
                                    ?.also {
                                        draggingItemKey = it.key as Long
                                        draggingItem = it
                                        delta = 0f
                                    }
                            },

                            onDrag = { change, dragAmount ->
                                change.consume()
                                delta += dragAmount.y

                                val liveItem = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.key == draggingItemKey }
                                    ?: return@detectDragGesturesAfterLongPress

                                // Always use liveItem for position calculations
                                val draggedItemMiddle = liveItem.offset + liveItem.size / 2f + delta

                                val reorderTarget = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                    if (item.key == draggingItemKey) return@firstOrNull false
                                    when {
                                        item.offset > liveItem.offset ->
                                            draggedItemMiddle - liveItem.size / 2f > item.offset + item.size - item.size * 0.35
                                        item.offset < liveItem.offset ->
                                            draggedItemMiddle + liveItem.size / 2f < item.offset + item.size * 0.35
                                        else -> false
                                    }
                                }

                                if (reorderTarget != null) {
                                    val oldLiveOffset = liveItem.offset

                                    // Null out Compose's internal key tracker BEFORE the list reorders.
                                    // Without this, LazyListState sees the first item's key moved to a new
                                    // index and auto-scrolls to follow it — fighting any correction we apply.
                                    listState.clearFirstItemKeyTracking()

                                    onMoveNote(draggingItemKey!!, reorderTarget.key as Long)

                                    // Compensate delta for the visual position change
                                    delta += oldLiveOffset - reorderTarget.offset

                                    // Update snapshot
                                    draggingItem = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == draggingItemKey }
                                }

                                // Combine target detection — use liveItem for updated middle
                                val updatedMiddle = liveItem.offset + liveItem.size / 2f + delta
                                val combineTarget = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                    if (item.key == draggingItemKey) return@firstOrNull false
                                    val itemMiddle = item.offset + item.size / 2f
                                    kotlin.math.abs(updatedMiddle - itemMiddle) < item.size * 0.25f
                                }
                                targetKey = combineTarget?.key as? Long

                                // Overscroll — no launch needed, trySend is non-suspending
                                val draggedTop = liveItem.offset + delta
                                val draggedBottom = draggedTop + liveItem.size
                                val listStart = listState.layoutInfo.viewportStartOffset.toFloat()
                                val listEnd = listState.layoutInfo.viewportEndOffset.toFloat()

                                val scrollAmount = when {
                                    draggedTop < listStart -> ((draggedTop - listStart) * 0.2f).coerceIn(-30f, 0f)
                                    draggedBottom > listEnd -> ((draggedBottom - listEnd) * 0.2f).coerceIn(0f, 30f)
                                    else -> 0f
                                }
                                if (scrollAmount != 0f) {
                                    scrollChannel.trySend(scrollAmount)
                                }
                            },

                            onDragEnd = {
                                val combineFrom = notes.firstOrNull { it.id == draggingItemKey }
                                val combineTo = notes.firstOrNull { it.id == targetKey }
                                if (combineFrom != null && combineTo != null) {
                                    onCombineNote(combineFrom, combineTo)
                                } else
                                    onDragEnd()

                                draggingItemKey = null
                                delta = 0f
                                targetKey = null
                            },

                            onDragCancel = {
                                draggingItemKey = null
                                delta = 0f
                                targetKey = null
                            }
                        )
                    }
                },
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

                SwipeableItemWithAction(
                    isRevealed = note.isRevealed,
                    actions = {
                        ActionIcon(
                            onClick = onDelete,
                            backgroundColor = Color.White,
                            icon = Icons.Default.Delete,
                            tint = Color.Black,
                            modifier = Modifier.fillMaxHeight()
                        )
                    },
                    onDragStart = onExpand,
                    modifier = if (draggingItemKey == note.id) Modifier
                        .padding(5.dp)
                        .zIndex(5f)
                        .graphicsLayer {
                            val listStart = listState.layoutInfo.viewportStartOffset.toFloat()
                            val listEnd = listState.layoutInfo.viewportEndOffset.toFloat()
                            val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == note.id }
                            val itemOffset = info?.offset?.toFloat() ?: 0f
                            val itemSize = info?.size?.toFloat() ?: 0f

                            val minDelta = listStart - itemOffset
                            val maxDelta = listEnd - itemOffset - itemSize

                            translationY = delta.coerceIn(minDelta-30, maxDelta+30)
                        } else if (targetKey == note.id)
                            Modifier.graphicsLayer {
                            scaleX = 1.05f
                            scaleY = 1.05f
                        }
                         else Modifier.animateItem()
                ) {
                    NoteItem(
                        note = note,
                        onClick = onClick,
                    )
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

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Add this outside your composable, at file level
private fun LazyListState.clearFirstItemKeyTracking() {
    try {
        val scrollPositionField = LazyListState::class.java
            .getDeclaredField("scrollPosition")
            .apply { isAccessible = true }
        val scrollPosition = scrollPositionField.get(this)

        val keyField = scrollPosition::class.java
            .getDeclaredField("lastKnownFirstItemKey")
            .apply { isAccessible = true }
        keyField.set(scrollPosition, null)
    } catch (_: Exception) {
        // Reflection failed (obfuscation/API change) — graceful no-op
    }
}
