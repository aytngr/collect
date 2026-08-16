package com.aytngr.feature.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aytngr.core.common.base.formatCreatedAt
import com.aytngr.core.common.base.formatDate
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.Spacing
import com.aytngr.core.designsystem.theme.CollectTheme
import com.aytngr.core.ui.NoteItem
import com.aytngr.core.ui.R
import com.aytngr.core.ui.ReminderBox
import com.aytngr.core.ui.VerticalSpacer
import com.aytngr.core.ui.noRippleClickable
import com.aytngr.domain.models.Note
import com.aytngr.feature.home.R as HomeR

@Composable
fun HomeScreen(
    onNavigateToNotesList: () -> Unit,
    onNavigateToNoteDetail: (noteId: Long?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
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
            .systemBarsPadding()
            .padding(Spacing.screen)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { /* TODO: navigate to search */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(HomeR.string.home_search_cd),
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

        VerticalSpacer(Spacing.lg)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.bell),
                    tint = AppTheme.colors.accent,
                    contentDescription = "",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(HomeR.string.home_up_next),
                    style = AppTextStyle.Eyebrow,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }

            Text(
                text = pluralStringResource(HomeR.plurals.home_reminders_count, state.reminderCount, state.reminderCount),
                style = AppTextStyle.Metadata,
                color = AppTheme.colors.faint
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        if (state.upNextReminders.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                itemsIndexed(items = state.upNextReminders) { index, reminder ->
                    ReminderBox(
                        isNext = (index == 0),
                        title = reminder.title,
                        date = reminder.reminderAt!!.formatCreatedAt()
                    )
                }
            }
        }

        VerticalSpacer(Spacing.xl)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(HomeR.string.home_jump_back_in),
                style = AppTextStyle.Eyebrow,
            )

            Text(
                text = stringResource(HomeR.string.home_all_notes),
                style = AppTextStyle.Button,
                color = AppTheme.colors.accent,
                modifier = Modifier.noRippleClickable() {
                    onNavigateToNotesList()
                }
            )
        }

        // todo list of recent notes
        state.recentNotes.let {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.sm),
                modifier = Modifier.weight(1f),

                ) {
                itemsIndexed(it) { index, note ->
                    NoteItem(
                        title = note.title,
                        content = note.content,
                        time = state.timeAgo[index],
                        category = note.category.name,
                        isPinned = note.isPinned,
                        reminder = null,
                        metadata = null,
                        onClick = { onNavigateToNoteDetail(note.id) },
                        onLongClick = {},
                    )
                }
            }
        }

        VerticalSpacer(Spacing.md)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = { onIntent(HomeContract.Intent.CreateNewNote) })
                .border(width = 1.dp, color = AppTheme.colors.line, shape = AppShapes.large)
                .clip(AppShapes.large)
                .background(color = Color.White)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        ) {
            Text(
                text = stringResource(HomeR.string.home_capture_placeholder),
                style = AppTextStyle.NoteTitle,
                color = AppTheme.colors.faint,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = AppTheme.colors.accent), onClick = { }) {
                Icon(
                    painter = painterResource(R.drawable.mic),
                    tint = Color.White,
                    contentDescription = ""
                )
            }

        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(Spacing.lg)
            )
        }

        if(state.showOverlayDialog){
            AlertDialog(
                onDismissRequest = { onIntent(HomeContract.Intent.RefreshOverlayStatus) },
                title = { Text(stringResource(HomeR.string.home_quick_capture_off_title)) },
                text = { Text(stringResource(HomeR.string.home_quick_capture_off_body)) },
                confirmButton = {
                    StartFloatingServiceButton({ onIntent(HomeContract.Intent.StartOverlay) })
                },
                dismissButton = { TextButton(onClick = { onIntent(HomeContract.Intent.RefreshOverlayStatus) }) { Text(stringResource(HomeR.string.home_not_now)) } },
            )
        }

    }
}

@Composable
fun StartFloatingServiceButton(startOverlay: () -> Unit) {
    val context = LocalContext.current.applicationContext

    Button(onClick = {
        if (Settings.canDrawOverlays(context)) {
            startOverlay()
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }
    }) {
        Text(stringResource(HomeR.string.home_start_floating_widget))
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


@Preview(showBackground = true)
@Composable
private fun ReminderBoxPreview() {
    CollectTheme {
        ReminderBox(true, "Standup - Thursday", "Today 9 AM")
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    CollectTheme {
        HomeContent(
            state = HomeContract.State(
                date = "Sunday, June 1",
                greeting = "Good morning, Aytan",
                recentNotes = emptyList(),
                upNextReminders = listOf(Note(title = "Hello", reminderAt = 264723647832647))
            ),
            onNavigateToNoteDetail = {},
            onNavigateToNotesList = {},
            onIntent = {}
        )
    }
}
