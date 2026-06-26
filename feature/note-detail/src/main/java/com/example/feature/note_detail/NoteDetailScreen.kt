package com.example.feature.note_detail

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTextStyle.DetailHeadline
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.core.ui.HorizontalSpacer
import com.example.core.ui.R
import com.example.core.ui.ReminderBox
import com.example.core.ui.ReminderSheet
import com.example.core.ui.VerticalSpacer
import com.example.core.ui.WeightSpacer
import com.example.core.ui.noRippleClickable
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.feature.note_detail.ops.copyToInternalStorage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun NoteDetailScreen(
    itemId: Long?,
    onBackClick: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP)
                viewModel.handleIntent(NoteDetailContract.Intent.SaveNow)
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(viewModel) {
        itemId?.let { viewModel.handleIntent(NoteDetailContract.Intent.LoadNote(it)) }
        viewModel.effects.collect { effect ->
            when (effect) {
                NoteDetailContract.Effect.NavigateBack -> onBackClick()
            }
        }
    }

    NoteDetailContent(state = state, viewModel::handleIntent)
}

@Composable
fun NoteDetailContent(
    state: NoteDetailContract.State,
    onIntent: (NoteDetailContract.Intent) -> Unit
) {

    var showImagePreview by remember { mutableIntStateOf(-1) }
    var showReminderSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (!granted)
            Toast.makeText(
                context,
                "Enable notifications to get reminders",
                Toast.LENGTH_SHORT
            ).show()
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = uri.copyToInternalStorage(context) ?: return@launch
            val current = state.note.images.orEmpty().filterNotNull()
            onIntent(NoteDetailContract.Intent.UpdateNote(images = current + path))
        }
    }


    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colors.bg)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { /* action */ }) {
                    Icon(
                        painter = painterResource(R.drawable.chevron),
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onIntent(NoteDetailContract.Intent.PinNote) }) {
                    Icon(//todo icon
                        painter = if (state.note.isPinned) painterResource(R.drawable.pin) else painterResource(
                            R.drawable.pin
                        ),
                        contentDescription = "Back"
                    )
                }
                IconButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showReminderSheet = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.bell),
                        contentDescription = "Back"
                    )
                }
                IconButton(onClick = { /* action */ }) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = "Back"
                    )
                }
            }

            VerticalSpacer(12.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.note.category.name,
                    style = AppTextStyle.Eyebrow,
                    color = AppTheme.colors.faint
                )

                HorizontalSpacer(8.dp)

                Text(
                    text = "·",
                    style = AppTextStyle.Eyebrow,
                    color = AppTheme.colors.faint
                )

                HorizontalSpacer(8.dp)

                Text(
                    text = state.createdTime,
                    style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint
                )
            }

            VerticalSpacer(12.dp)

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = DetailHeadline,
                value = state.note.title,
                onValueChange = {
                    onIntent(
                        NoteDetailContract.Intent.UpdateNote(
                            title = it
                        )
                    )
                },
                decorationBox = { innerTextField ->
                    Box {
                        if (state.note.title.isEmpty()) {
                            Text(
                                text = "Untitled",
                                style = DetailHeadline,
                                color = AppTheme.colors.faint
                            )
                        }
                        innerTextField()
                    }
                }
            )

            state.note.reminderAt?.let {
                ReminderBox(
                    isNext = it > System.currentTimeMillis(),
                    title = "REMINDER",
                    date = state.reminder,
                    isDetail = true,
                    onEditClick = { showReminderSheet = true }
                )
            }

            VerticalSpacer(16.dp)

            BasicTextField(
                textStyle = AppTextStyle.NoteBody,
                value = state.note.content,
                onValueChange = {
                    onIntent(
                        NoteDetailContract.Intent.UpdateNote(
                            content = it
                        )
                    )
                },
                decorationBox = { innerTextField ->
                    Box() {
                        if (state.note.content.isEmpty()) {
                            Text(
                                text = "Enter your note...",
                                style = AppTextStyle.NoteBody,
                                color = AppTheme.colors.faint
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            VerticalSpacer(16.dp)

            state.note.images?.let { images ->
                VerticalSpacer(16.dp)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(items = images) { index, image ->
                        AsyncImage(
                            model = image?.let(::File),
                            contentDescription = "Screenshot",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(AppShapes.small)
                                .noRippleClickable() { showImagePreview = index },
                            contentScale = ContentScale.Crop,
                            onError = {
                                Log.e(
                                    "noteimg",
                                    "load failed: $image",
                                    it.result.throwable
                                )
                            },
                        )
                    }
                    item {
                        AddImage(onClick = {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        })
                    }
                }
            }

            WeightSpacer()

            VerticalSpacer(8.dp)

            Text(
                text = state.editedTime,
                style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )

            VerticalSpacer(8.dp)

            if (showImagePreview != -1 && state.note?.images != null) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = 32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            bitmap = BitmapFactory.decodeFile(state.note?.images!![showImagePreview])
                                .asImageBitmap(),
                            contentDescription = "Full Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        TextButton(
                            onClick = { showImagePreview = -1 },
                        ) {
                            Text("Close", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        if (showReminderSheet) {
            ReminderSheet(
                currentReminderAt = state.note.reminderAt,
                onSet = {
                    onIntent(NoteDetailContract.Intent.SetReminder(it)); showReminderSheet = false
                },
                onRemove = {
                    onIntent(NoteDetailContract.Intent.ClearReminder); showReminderSheet = false
                },
                onDismiss = { showReminderSheet = false },
            )
        }
    }
}

@Composable
private fun AddImage(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(
                shape = AppShapes.small,
                width = 1.dp, color = AppTheme.colors.line,
            )
            .size(100.dp)
            .noRippleClickable(onClick = { onClick() }),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Icon(
                imageVector = Icons.Outlined.Add,
                tint = AppTheme.colors.accent,
                contentDescription = "Add Image"
            )
            Text("Add", color = AppTheme.colors.accent, style = AppTextStyle.Metadata)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun NoteDetailContentPreview() {
    TaskFlowTheme {
        NoteDetailContent(
            state = NoteDetailContract.State(
                note = Note(
                    id = 1L,
                    title = "Team Standup Notes",
                    content = "Discussed sprint progress.\nBlocking issue: auth service timeout.\nNext steps: investigate logs and schedule a sync with backend team.",
                    category = NoteCategory.WORK,
                    language = Language.ENGLISH,
                    createdAt = System.currentTimeMillis(),
                    extractedData = emptyMap(),
                    images = listOf("lalalalla")
                ),
                createdTime = "Yesterday 05:00",
                isLoading = false
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteDetailContentLoadingPreview() {
    TaskFlowTheme {
        NoteDetailContent(
            state = NoteDetailContract.State(
                isLoading = true,
                note = Note()
            ),
            onIntent = {}
        )
    }
}