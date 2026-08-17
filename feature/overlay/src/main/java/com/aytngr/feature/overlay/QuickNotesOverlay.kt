package com.aytngr.feature.overlay

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTextStyle.DetailHeadline
import com.aytngr.core.designsystem.theme.AppTextStyle.NoteBody
import com.aytngr.core.designsystem.theme.AppTextStyle.NoteTitle
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.Sub
import com.aytngr.core.designsystem.theme.CollectTheme
import com.aytngr.core.ui.AppCircle
import com.aytngr.core.ui.HorizontalSpacer
import com.aytngr.core.ui.R
import com.aytngr.core.ui.ReminderSheet
import com.aytngr.core.ui.ReminderSheetContent
import com.aytngr.core.ui.VerticalSpacer
import com.aytngr.core.ui.WeightSpacer
import com.aytngr.core.ui.noRippleClickable
import com.aytngr.domain.models.NoteCategory
import com.aytngr.domain.models.SaveStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import com.aytngr.feature.overlay.R as OverlayR

@Composable
fun QuickNotesOverlay(
    text: String,
    category: String,
    reminderAt: Long?,
    onSave: suspend (String, List<String>, (SaveStatus) -> Unit) -> Unit,
    onTextChange: (String) -> Unit,
    onRemoveScreenshot: (Int) -> Unit,
    onClose: () -> Unit,
    onSetReminder: (Long) -> Unit,
    onRemoveReminder: () -> Unit,
    onHide: () -> Unit,
    onExpand: () -> Unit,
    onAddScreenshot: () -> Unit,
    onClickCategory: () -> Unit,
    screenshots: List<String> = emptyList(),
) {
    var showImagePreview by remember { mutableIntStateOf(-1) }
    var showReminderSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = AppTheme.colors.bg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            var saveStatus by remember { mutableStateOf<SaveStatus?>(null) }
            saveStatus?.let {
                when (it) {
                    SaveStatus.SUCCESS -> Text(stringResource(OverlayR.string.overlay_saved), color = Color.Green)
                    SaveStatus.ERROR -> Text(stringResource(OverlayR.string.overlay_error), color = Color.Red)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,

                ) {
                AppCircle()
                HorizontalSpacer(8.dp)
                Text(
                    text = stringResource(OverlayR.string.overlay_collect_label),
                    style = NoteBody
                )
                HorizontalSpacer(8.dp)
                Text(
                    "·",
                    color = AppTheme.colors.faint
                )
                HorizontalSpacer(4.dp)
                Text(
                    text = stringResource(OverlayR.string.overlay_quick_capture_label),
                    style = AppTextStyle.Metadata,
                    color = AppTheme.colors.faint
                )
                WeightSpacer()
                IconButton(
                    onClick = onExpand,
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceAlt)
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_up_right),
                        contentDescription = stringResource(OverlayR.string.overlay_hide_cd)
                    )
                }

                HorizontalSpacer(4.dp)
                IconButton(
                    onClick = onHide,
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceAlt)
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.minimize),
                        contentDescription = stringResource(OverlayR.string.overlay_minimize_cd)
                    )
                }

                HorizontalSpacer(4.dp)
                IconButton(
                    onClick = onClose,
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceAlt)
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.x),
                        contentDescription = stringResource(OverlayR.string.overlay_close_cd)
                    )
                }
            }


            VerticalSpacer(8.dp)

            BasicTextField(

                modifier = Modifier.fillMaxWidth(),
                textStyle = NoteBody,
                value = text,
                onValueChange = onTextChange,
                maxLines = 2,
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = stringResource(OverlayR.string.overlay_text_placeholder),
                                style = NoteBody,
                                color = AppTheme.colors.faint
                            )
                        }
                        innerTextField()
                    }
                })


            VerticalSpacer(8.dp)

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(screenshots) { index, screenshot ->

                        Box(Modifier.clip(AppShapes.small)) {
                            AsyncImage(
                                model = File(screenshot),
                                contentDescription = stringResource(OverlayR.string.overlay_screenshot_thumbnail_cd),
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(AppShapes.small)
                                    .noRippleClickable() { showImagePreview = index },
                                contentScale = ContentScale.Crop,
                                onError = {
                                    Log.e(
                                        "noteimg",
                                        "load failed: $screenshot",
                                        it.result.throwable
                                    )
                                },
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable(onClick = {
                                        onRemoveScreenshot(index)
                                    })
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Icon(
                                    modifier = Modifier.padding(5.dp),
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = stringResource(OverlayR.string.overlay_remove_screenshot_cd),
                                    tint = Color.Black
                                )
                            }
                        }
                }
            }

            if(showReminderSheet){
                VerticalSpacer(8.dp)
                ReminderSheet(
                    currentReminderAt = reminderAt,
                    onSet = onSetReminder,
                    onRemove = onRemoveReminder,
                    onDismiss = {},
                    forQuickNoteOverlay = true
                )
            }

            VerticalSpacer(12.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = AppTheme.colors.line
                        )
                        .size(36.dp)
                        .padding(10.dp), onClick = {
                        onAddScreenshot()
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.camera),
                        tint = AppTheme.colors.sub,
                        contentDescription = stringResource(OverlayR.string.overlay_add_screenshot_cd)
                    )
                }
                HorizontalSpacer(8.dp)
                IconButton(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = AppTheme.colors.line
                        )
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color = if (reminderAt != null) AppTheme.colors.accent else Color.Transparent)
                        .padding(10.dp),
                    onClick = {
                        showReminderSheet = !showReminderSheet
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.bell),
                        tint = if (reminderAt != null) Color.White else AppTheme.colors.sub,
                        contentDescription = stringResource(OverlayR.string.overlay_reminder_cd)
                    )
                }
                HorizontalSpacer(8.dp)
                Row(
                    modifier = Modifier
                        .border(1.dp, AppTheme.colors.line, shape = AppShapes.large)
                        .clip(AppShapes.large)
                        .clickable { onClickCategory() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.tag),
                        tint = AppTheme.colors.sub,
                        contentDescription = stringResource(OverlayR.string.overlay_category_cd),
                        modifier = Modifier.size(18.dp)
                    )
                    HorizontalSpacer(4.dp)
                    Text(
                        text = category.lowercase().replaceFirstChar { it.uppercase() },
                        style = AppTextStyle.BodySnippet,
                        color = AppTheme.colors.sub
                    )
                }


                WeightSpacer()

                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            color = AppTheme.colors.accent
                        ),
                    onClick = {
                        coroutineScope.launch {
                            onSave(text, screenshots) {
                                saveStatus = it
                            }
                            delay(1000)
                            saveStatus = null
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_up),
                        tint = Color.White,
                        contentDescription = stringResource(OverlayR.string.overlay_save_cd)
                    )
                }
            }
        }
    }
    if (showImagePreview in screenshots.indices) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.9f))
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = File(screenshots[showImagePreview]),
                    contentDescription = stringResource(OverlayR.string.overlay_full_screenshot_cd),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showImagePreview = -1 }) {
                    Text(stringResource(OverlayR.string.overlay_close_button), color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun QuickNotes() {
    CollectTheme {
        QuickNotesOverlay(
            text = "Hello",
            onSave = { _, _, _ -> },
            onClose = {},
            onExpand = {},
            onHide = {},
            onTextChange = {},
            onRemoveScreenshot = {},
            onAddScreenshot = {},
            screenshots = emptyList(),
            category = NoteCategory.GENERAL.name,
            onClickCategory = {},
            reminderAt = null,
            onSetReminder = {},
            onRemoveReminder = {}
        )
    }

}