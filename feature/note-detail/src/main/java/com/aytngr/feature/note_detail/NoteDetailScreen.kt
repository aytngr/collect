package com.aytngr.feature.note_detail

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTextStyle.DetailHeadline
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.Spacing
import com.aytngr.core.designsystem.theme.CollectTheme
import com.aytngr.core.ui.BackButton
import com.aytngr.core.ui.createdAtLabel
import com.aytngr.core.ui.editedLabel
import com.aytngr.core.ui.HorizontalSpacer
import com.aytngr.core.ui.R
import com.aytngr.core.ui.ReminderBox
import com.aytngr.core.ui.ReminderSheet
import com.aytngr.core.ui.VerticalSpacer
import com.aytngr.core.ui.WeightSpacer
import com.aytngr.core.ui.noRippleClickable
import com.aytngr.domain.models.Note
import com.aytngr.domain.models.NoteCategory
import com.aytngr.feature.note_detail.ops.copyToInternalStorage
import kotlinx.coroutines.launch
import java.io.File
import com.aytngr.feature.note_detail.R as NoteDetailR
import com.aytngr.core.ui.labelRes

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

    BackHandler(enabled = true) {
        viewModel.handleIntent(NoteDetailContract.Intent.SaveNow)
        onBackClick()
    }

    NoteDetailContent(state = state, onIntent = viewModel::handleIntent, onBack = onBackClick)
}

@Composable
fun NoteDetailContent(
    state: NoteDetailContract.State,
    onIntent: (NoteDetailContract.Intent) -> Unit,
    onBack: () -> Unit = {}
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
                context.getString(NoteDetailR.string.notedetail_toast_enable_notifications),
                Toast.LENGTH_SHORT
            ).show()
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = uri.copyToInternalStorage(context) ?: return@launch
            val current = state.note.images
            onIntent(NoteDetailContract.Intent.UpdateNote(images = current + path))
        }
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        onIntent(NoteDetailContract.Intent.SchedulePendingReminder)
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                .systemBarsPadding()
                .imePadding()
                .padding(Spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onIntent(NoteDetailContract.Intent.PinNote) }) {
                    Icon(//todo icon
                        painter = if (state.note.isPinned) painterResource(R.drawable.pinned) else painterResource(
                            R.drawable.pin
                        ),
                        contentDescription = stringResource(NoteDetailR.string.notedetail_pin_cd)
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
                        contentDescription = stringResource(NoteDetailR.string.notedetail_reminder_cd)
                    )
                }
                IconButton(onClick = { /* action */ }) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = stringResource(NoteDetailR.string.notedetail_more_cd)
                    )
                }
            }

            VerticalSpacer(Spacing.md)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(state.note.category.labelRes()),
                    style = AppTextStyle.Eyebrow,
                    color = AppTheme.colors.faint
                )

                HorizontalSpacer(Spacing.sm)

                Text(
                    text = "·",
                    style = AppTextStyle.Eyebrow,
                    color = AppTheme.colors.faint
                )

                HorizontalSpacer(Spacing.sm)

                Text(
                    text = createdAtLabel(state.note.createdAt),
                    style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint
                )
            }

            VerticalSpacer(Spacing.md)

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
                                text = stringResource(NoteDetailR.string.notedetail_untitled),
                                style = DetailHeadline,
                                color = AppTheme.colors.faint
                            )
                        }
                        innerTextField()
                    }
                }
            )

            state.note.reminderAt?.let {
                VerticalSpacer(Spacing.md)
                ReminderBox(
                    isNext = it > System.currentTimeMillis(),
                    title = stringResource(NoteDetailR.string.notedetail_reminder_label),
                    date = createdAtLabel(it),
                    isDetail = true,
                    onEditClick = { showReminderSheet = true }
                )
            }

            VerticalSpacer(Spacing.lg)

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
                                text = stringResource(NoteDetailR.string.notedetail_content_placeholder),
                                style = AppTextStyle.NoteBody,
                                color = AppTheme.colors.faint
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )

            VerticalSpacer(Spacing.lg)

            state.note.images.let { images ->
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(items = images) { index, image ->
                        AsyncImage(
                            model = image?.let(::File),
                            contentDescription = stringResource(NoteDetailR.string.notedetail_screenshot_cd),
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

            VerticalSpacer(Spacing.sm)

            Text(
                text = editedLabel(state.note.updatedAt),
                style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )

            VerticalSpacer(Spacing.sm)

            if (showImagePreview != -1 && state.note?.images != null) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = Spacing.xxl)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = File(state.note.images[showImagePreview]),
                            contentDescription = stringResource(NoteDetailR.string.notedetail_screenshot_cd),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            contentScale = ContentScale.Fit,
                        )
                        TextButton(
                            onClick = { showImagePreview = -1 },
                        ) {
                            Text(stringResource(NoteDetailR.string.notedetail_close), color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        if (showReminderSheet) {
            ReminderSheet(
                currentReminderAt = state.note.reminderAt,
                onSet = {
                    showReminderSheet = false
                    onIntent(NoteDetailContract.Intent.SetPendingReminder(it))
                    onIntent(NoteDetailContract.Intent.CheckExactAlarmPermission)
                },
                onRemove = {
                    onIntent(NoteDetailContract.Intent.ClearReminder); showReminderSheet = false
                },
                onDismiss = { showReminderSheet = false },
            )

        }

        if (state.showExactAlarmPermissionDialog) {
            AlertDialog(
                onDismissRequest = { onIntent(NoteDetailContract.Intent.RefreshPermissionDialogVisibility) },
                title = { Text(stringResource(NoteDetailR.string.notedetail_exact_alarm_title)) },
                text = { Text(stringResource(NoteDetailR.string.notedetail_exact_alarm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        exactAlarmLauncher.launch(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                "package:${context.packageName}".toUri()
                            )
                        )
                        onIntent(NoteDetailContract.Intent.RefreshPermissionDialogVisibility)
//                        onIntent(NoteDetailContract.Intent.SchedulePendingReminder)
                    }) {
                        Text(stringResource(NoteDetailR.string.notedetail_open_settings))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onIntent(NoteDetailContract.Intent.RefreshPermissionDialogVisibility)
                        onIntent(NoteDetailContract.Intent.SchedulePendingReminder)
                    }) {
                        Text(stringResource(NoteDetailR.string.notedetail_cancel))
                    }
                },
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
                contentDescription = stringResource(NoteDetailR.string.notedetail_add_image_cd)
            )
            Text(stringResource(NoteDetailR.string.notedetail_add_label), color = AppTheme.colors.accent, style = AppTextStyle.Metadata)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun NoteDetailContentPreview() {
    CollectTheme {
        NoteDetailContent(
            state = NoteDetailContract.State(
                note = Note(
                    id = 1L,
                    title = "Team Standup Notes",
                    content = "Discussed sprint progress.\nBlocking issue: auth service timeout.\nNext steps: investigate logs and schedule a sync with backend team.",
                    category = NoteCategory.WORK,
                    createdAt = System.currentTimeMillis(),
                    extractedData = emptyMap(),
                    images = listOf("lalalalla")
                ),
                isLoading = false
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteDetailContentLoadingPreview() {
    CollectTheme {
        NoteDetailContent(
            state = NoteDetailContract.State(
                isLoading = true,
                note = Note()
            ),
            onIntent = {}
        )
    }
}