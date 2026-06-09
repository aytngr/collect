package com.example.feature.note_detail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTextStyle.DetailHeadline
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.core.ui.HorizontalSpacer
import com.example.core.ui.R
import com.example.core.ui.RecordingAnimation
import com.example.core.ui.RecordingState
import com.example.core.ui.VerticalSpacer
import com.example.core.ui.WeightSpacer
import com.example.domain.models.Language
import com.example.domain.models.Note
import com.example.domain.models.NoteCategory
import com.example.domain.models.TextBlock

@Composable
fun NoteDetailScreen(
    itemId: Long?,
    onBackClick: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver{ _, e ->
            if(e == Lifecycle.Event.ON_STOP)
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
//    var content by remember { mutableStateOf(state.note.content) }
//    var title by remember { mutableStateOf(state.note.title) }


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
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { /* action */ }) {
                    Icon(
                        painter = painterResource(R.drawable.chevron),
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* action */ }) {
                    Icon(
                        painter = painterResource(R.drawable.pin),
                        contentDescription = "Back"
                    )
                }
                IconButton(onClick = { /* action */ }) {
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

            VerticalSpacer(16.dp)

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = DetailHeadline,
                value = state.note.title,
                onValueChange = {
//                    onIntent(
//                        NoteDetailContract.Intent.ChangeText(
//                            note = state.note!!,
//                            title = it
//                        )
//                    )
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
            state.note?.images?.let { images ->
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(items = images) { index, image ->
                        Image(
                            bitmap = BitmapFactory.decodeFile(image).asImageBitmap(),
                            contentDescription = "Screenshot",
                            modifier = Modifier
                                .width(120.dp)
                                .wrapContentHeight()
                                .clickable(
                                    onClick = { showImagePreview = index }
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            VerticalSpacer(16.dp)

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(items = state.note.textBlocks, key = { it.id }) { block ->
                    when (block) {
                        is TextBlock.PlainText -> {
                            BasicTextField(
                                textStyle = AppTextStyle.NoteBody,
                                value = state.fieldValues[block.id] ?: TextFieldValue(text = block.text, selection = TextRange(block.text.length)),
                                onValueChange = {
                                    onIntent(
                                        NoteDetailContract.Intent.UpdateTextBlock(
                                            textBlockId = block.id,
                                            field = it
                                        )
                                    )
                                },
                                modifier = Modifier.onFocusChanged{
                                    if(it.isFocused) onIntent(NoteDetailContract.Intent.FocusBlock(textBlockId = block.id))
                                },
                                decorationBox = { innerTextField ->
                                    Box(Modifier.fillMaxSize()) {
                                        if (block.text.isEmpty()) {
                                            Text(
                                                text = "Enter your note...",
                                                style = AppTextStyle.NoteBody,
                                                color = AppTheme.colors.faint
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        is TextBlock.VoiceText -> {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                VerticalDivider(
                                    thickness = 1.dp,
                                    color = AppTheme.colors.accentSoft,
                                )
                                HorizontalSpacer(8.dp)
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(color = AppTheme.colors.accent)
                                                .size(26.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.play),
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp),
                                                contentDescription = ""
                                            )
                                        }
                                        HorizontalSpacer(8.dp)

                                        Text(
                                            "VOICE",
                                            style = AppTextStyle.Eyebrow,
                                            color = AppTheme.colors.accent
                                        )
                                        HorizontalSpacer(8.dp)
                                        Text(
                                            "0:12",
                                            style = AppTextStyle.Metadata,
                                            color = AppTheme.colors.faint
                                        )

                                    }
                                    VerticalSpacer(8.dp)
                                    BasicTextField(
                                        textStyle = AppTextStyle.NoteBody,
                                        value = state.fieldValues[block.id] ?: TextFieldValue(text = block.text, selection = TextRange(block.text.length)),
                                        onValueChange = {
                                            onIntent(
                                                NoteDetailContract.Intent.UpdateTextBlock(
                                                    textBlockId = block.id,
                                                    field = it
                                                )
                                            )
                                        },
                                        modifier = Modifier.onFocusChanged{
                                            if(it.isFocused) onIntent(NoteDetailContract.Intent.FocusBlock(textBlockId = block.id))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }


            HorizontalDivider(
                thickness = 1.dp,
                color = AppTheme.colors.line
            )
            VerticalSpacer(8.dp)

            when (state.recordingState) {
                RecordingState.STOPPED -> {
                    BottomTab(
                        onRecordClick = {
                            onIntent(
                                NoteDetailContract.Intent.ChangeRecordingState(
                                    RecordingState.RECORDING
                                )
                            )
                        },
                        onCameraClick = {},
                        onFileClick = {},
                    )
                }

                else -> RecordingBottomTab(
                    state.recordingState,
                    state.recordingTime,
                    onRecordingStateChange = { v ->
                        onIntent(
                            NoteDetailContract.Intent.ChangeRecordingState(
                                v
                            )
                        )
                    })
            }

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
    }
}

@Composable
fun BottomTab(onRecordClick: () -> Unit, onCameraClick: () -> Unit, onFileClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    ) {
        IconButton(onClick = { onCameraClick() }) {
            Icon(
                painter = painterResource(R.drawable.camera),
                contentDescription = ""
            )
        }
        IconButton(onClick = { onFileClick() }) {
            Icon(
                painter = painterResource(R.drawable.attach),
                contentDescription = ""
            )
        }
        Spacer(Modifier.weight(1f))

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .background(color = AppTheme.colors.accent), onClick = { onRecordClick() }) {
            Icon(
                painter = painterResource(R.drawable.mic),
                tint = Color.White,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun RecordingBottomTab(
    recordingState: RecordingState,
    recordingTime: String,
    onRecordingStateChange: (RecordingState) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            //animating icon
            Text(
                text = "DICTATING",
                style = AppTextStyle.Eyebrow,
                color = AppTheme.colors.accent,
            )
            HorizontalSpacer(8.dp)
            Text(
                text = "inserting at cursor",
                style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )

            WeightSpacer()

            Text(
                text = recordingTime,
                color = AppTheme.colors.accent,
                style = AppTextStyle.VoiceReminderPill
            )
        }

        VerticalSpacer(16.dp)
        Row(Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.weight(1f)) {
                RecordingAnimation(recordingState == RecordingState.RECORDING || recordingState == RecordingState.RESUMED)
            }
            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(width = 1.dp, color = AppTheme.colors.line, shape = CircleShape),
                onClick = {

                    onRecordingStateChange(
                        when (recordingState) {
                            RecordingState.PAUSED -> RecordingState.RESUMED
                            RecordingState.RESUMED, RecordingState.RECORDING -> RecordingState.PAUSED
                            else -> RecordingState.PAUSED
                        }
                    )
                }) {
                Icon(
                    painter =
                        when (recordingState) {
                            RecordingState.PAUSED -> painterResource(R.drawable.play)
                            RecordingState.RESUMED, RecordingState.RECORDING -> painterResource(R.drawable.pause)
                            else -> painterResource(R.drawable.mic)
                        },
                    tint = AppTheme.colors.sub,
                    contentDescription = ""
                )
            }

            HorizontalSpacer(8.dp)
            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = AppTheme.colors.accent),
                onClick = { onRecordingStateChange(RecordingState.STOPPED) }) {
                Icon(
                    painter = painterResource(R.drawable.stop),
                    tint = Color.White,
                    contentDescription = ""
                )
            }
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
//                    content = "Discussed sprint progress.\nBlocking issue: auth service timeout.\nNext steps: investigate logs and schedule a sync with backend team.",
                    category = NoteCategory.WORK,
                    language = Language.ENGLISH,
                    textBlocks = listOf(
                        TextBlock.PlainText(id = "1", "Hello"), TextBlock.VoiceText(
                            id = "2", "My voice note", durationMs = 1
                        )
                    ),
                    createdAt = System.currentTimeMillis(),
                    extractedData = emptyMap()
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
    TaskFlowTheme {
        NoteDetailContent(
            state = NoteDetailContract.State(isLoading = true, note = Note(textBlocks = emptyList())),
            onIntent = {}
        )
    }
}