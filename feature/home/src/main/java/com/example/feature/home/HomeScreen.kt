package com.example.feature.home

import android.Manifest
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.home.components.LanguageSelector
import com.example.feature.home.components.RecentNotesSection
import com.example.feature.home.components.StatusSection
import com.example.feature.home.components.VoiceButton
import androidx.core.net.toUri
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.TaskFlowTheme
import com.example.core.ui.NoteItem
import com.example.core.ui.R
import com.example.core.ui.VerticalSpacer

@Composable
fun HomeScreen(onNavigateToNotesList: () -> Unit, onNavigateToNoteDetail: (noteId: Long?) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.handleIntent(HomeContract.Intent.StartVoiceRecognition)
        } else {
            // Handle permission denied
            viewModel.handleIntent(HomeContract.Intent.ClearTranscription)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeContract.Effect.RequestMicrophonePermission -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                is HomeContract.Effect.NavigateToNoteDetail -> onNavigateToNoteDetail(effect.id)

                is HomeContract.Effect.ShowError -> Toast.makeText(
                    context,
                    effect.error,
                    Toast.LENGTH_SHORT
                )

                is HomeContract.Effect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    HomeContent(
        state = state,
        onIntent = viewModel::handleIntent,
        onNavigateToNotesList = onNavigateToNotesList,
        onNavigateToNoteDetail = onNavigateToNoteDetail,
    )
}

@Composable
private fun HomeContent(
    state: HomeContract.State,
    onIntent: (HomeContract.Intent) -> Unit,
    onNavigateToNoteDetail: (Long?) -> Unit,
    onNavigateToNotesList: () -> Unit
) {

    val context = LocalContext.current
    if (!LocalInspectionMode.current && !Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .background(color = AppTheme.colors.bg)
            .fillMaxSize()
            .padding(16.dp)
    ) {
//        StartFloatingServiceButton()
//        BatteryButton()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = state.date,
                    style = AppTextStyle.Eyebrow,
                    color = AppTheme.colors.faint
                )
                Text(
                    text = state.greeting,
                    style = AppTextStyle.GreetingTitle,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.padding(top = 4.dp)
            ) {
                IconButton(onClick = { /* TODO: navigate to search */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = AppTheme.colors.ink
                    )
                }
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceAlt),
                    onClick = {}
                ) {
                    Text(
                        text = "A",
                        style = AppTextStyle.Button,
                        color = AppTheme.colors.ink
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.bell),
                    tint = AppTheme.colors.accent,
                    contentDescription = "",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "UP NEXT",
                    style = AppTextStyle.Eyebrow,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = "${state.reminderCount} reminders",
                style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        //todo list of reminders
        ReminderBox(true, "Standup - Thursday", "Today 9 AM")
        ReminderBox(false, "Standup - Thursday", "Today 9 AM")

        VerticalSpacer(16.dp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

            Text(
                text = "JUMP BACK IN",
                style = AppTextStyle.Eyebrow,
                modifier = Modifier.padding(start = 8.dp)
            )

            Text(
                text = "All notes",
                style = AppTextStyle.Button,
                color = AppTheme.colors.accent
            )
        }

        // todo list of recent notes
        state.recentNotes.let {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                modifier = Modifier.weight(1f),

                ) {
                items(it) { note ->
                    NoteItem(
                        title = note.title,
                        time = note.timePassed,
                        category = note.category.name,
                        isFav = note.isFav,
                        recording = null,
                        reminder = null,
                        metadata = null,
                        onClick = {},
                        modifier = Modifier
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { onIntent(HomeContract.Intent.CreateNewNote) })
                .border(width = 1.dp, color = AppTheme.colors.line, shape = AppShapes.large)
                .clip(AppShapes.large)
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Capture a thought...",
                style = AppTextStyle.NoteTitle,
                color = AppTheme.colors.faint,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = AppTheme.colors.accent), onClick = {  }) {
                Icon(
                    painter = painterResource(R.drawable.mic),
                    tint = Color.White,
                    contentDescription = ""
                )
            }

        }


//        LanguageSelector(
//            selectedLanguage = state.selectedLanguage,
//            onLanguageSelected = { language ->
//                onIntent(HomeContract.Intent.ChangeLanguage(language))
//            }
//        )
        Spacer(modifier = Modifier.height(32.dp))

//        VoiceButton(
//            voiceState = state.voiceState,
//            volumeLevel = state.volumeLevel,
//            onClick = {
//                when (state.voiceState) {
//                    HomeContract.VoiceState.Idle -> {
//                        onIntent(HomeContract.Intent.StartVoiceRecognition)
//                    }
//
//                    else -> {
//                        onIntent(HomeContract.Intent.StopVoiceRecognition)
//                    }
//                }
//            }
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        StatusSection(
//            state = state,
//            onRetry = { onIntent(HomeContract.Intent.RetryVoiceRecognition) },
//            onClear = { onIntent(HomeContract.Intent.ClearTranscription) }
//        )
//        Spacer(modifier = Modifier.height(32.dp))


        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun StartFloatingServiceButton() {
    val context = LocalContext.current.applicationContext

    Button(onClick = {
        if (Settings.canDrawOverlays(context)) {
//            val intent = Intent(context, OverlayService::class.java)
//            context.startService(intent)
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }
    }) {
        Text("Start Floating Widget")
    }
}

//@Composable
//fun BatteryButton() {
//    val context = LocalContext.current.applicationContext
//
//    Button(onClick = {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
//            if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
//                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
//                    Intent.setData = Uri.parse("package:${context.packageName}")
//                }
//                try {
//                    context.startActivity(intent)
//                } catch (e: Exception) {
//                    // Some devices don't support this
//                }
//            }
//        }
//    }) {
//        Text("for battery")
//    }
//}

@Composable
fun ReminderBox(
    isNext: Boolean,
    title: String,
    date: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isNext) AppTheme.colors.accent else AppTheme.colors.line,
                shape = AppShapes.medium
            )
            .clip(AppShapes.medium)
            .background(if (isNext) AppTheme.colors.accent.copy(alpha = 0.04f) else Color.White)
            .clickable(onClick = {})
            .padding(vertical = 16.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isNext) AppTheme.colors.accentSoft else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isNext) Color.Transparent else AppTheme.colors.line,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bell),
                tint = if (isNext) AppTheme.colors.accent else AppTheme.colors.sub,
                contentDescription = "",
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(text = title, style = AppTextStyle.NoteTitle)
            Text(text = date, style = AppTextStyle.Metadata, color = AppTheme.colors.faint)
        }
        Icon(
            painter = painterResource(id = R.drawable.chevron),
            tint = AppTheme.colors.faint,
            contentDescription = "",
            modifier = Modifier.size(18.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ReminderBoxPreview() {
    TaskFlowTheme {
        ReminderBox(true, "Standup - Thursday", "Today 9 AM")
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    TaskFlowTheme {
        HomeContent(
            state = HomeContract.State(
                date = "Sunday, June 1",
                greeting = "Good morning, Aytan",
                recentNotes = emptyList()
            ),
            onNavigateToNoteDetail = {},
            onNavigateToNotesList = {},
            onIntent = {}
        )
    }
}
