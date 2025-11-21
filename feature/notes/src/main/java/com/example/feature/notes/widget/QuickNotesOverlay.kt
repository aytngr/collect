package com.example.feature.notes.widget

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.rounded.Clear
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.domain.models.SaveStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.scale

@Composable
fun QuickNotesOverlay(
    text: String,
    onSave: suspend (String, List<Bitmap?>?, (SaveStatus) -> Unit) -> Unit,
    onClose: () -> Unit,
    onHide: () -> Unit,
    onScreenshot: (String, List<Bitmap>?) -> Unit,
    screenshots: List<Bitmap>? = null,
) {
    var showImagePreview by remember { mutableIntStateOf(-1) }
    var currentScreenshots by remember(screenshots) { mutableStateOf(screenshots?.toList())}
    var text by remember { mutableStateOf(text) }
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            var saveStatus by remember { mutableStateOf<SaveStatus?>(null) }
            saveStatus?.let {
                when (it) {
                    SaveStatus.SUCCESS -> Text("Saved!", color = Color.Green)
                    SaveStatus.ERROR -> Text("Error!", color = Color.Red)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Note",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    onHide()
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Screenshot"
                    )
                }
            }

            LazyRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                currentScreenshots?.let {
                    itemsIndexed(it) { index, screenshot ->
                        Box(modifier = Modifier.size(width = 70.dp, height = 110.dp)){
                            Image(
                                bitmap = screenshot.asImageBitmap(),
                                contentDescription = "Screenshot",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .size(width = 60.dp, height = 100.dp)
                                    .clickable { showImagePreview = index }
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.FillBounds
                            )
                            Box(
                                modifier = Modifier.size(28.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable(onClick = {
                                        // Remove by index — safe and reliable
                                        currentScreenshots = it.filterIndexed { i, _ -> i != index }.toMutableList()
                                        // If now empty, set to null to hide previews properly
                                        if (currentScreenshots.isNullOrEmpty()) {
                                            currentScreenshots = null
                                        }
                                        // Adjust preview index if needed
                                        if (showImagePreview >= (currentScreenshots?.size ?: 0)) {
                                            showImagePreview = -1
                                        }
                                    })
                                    .clip(CircleShape).
                                background(Color.White)
                            ) {
                                Icon(
                                    modifier = Modifier.padding(5.dp),
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Close",
                                    tint = Color.Black
                                )
                            }
                        }

                    }
                }
                item {
                    IconButton(onClick = {
                        onScreenshot(text, currentScreenshots)
                    }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Screenshot"
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Note") })
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onClose) { Text("Close") }
                Button(onClick = {
                    coroutineScope.launch {
                        onSave(text, currentScreenshots) {
                            saveStatus = it
                            if(it == SaveStatus.SUCCESS){
                                text = ""
                                currentScreenshots = emptyList()
                            }
                        }
                        delay(1000)
                        saveStatus = null
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
    if (showImagePreview != -1 && currentScreenshots != null) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.9f))
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = currentScreenshots!![showImagePreview].asImageBitmap(),
                    contentDescription = "Full Screenshot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showImagePreview = -1 }) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun QuickNotes(){
    QuickNotesOverlay(
        text = "Hello",
        onSave = {_,_,_ ->},
        onClose = {},
        onHide = {},
        onScreenshot = {_,_ ->}
    )
}