package com.example.feature.notes.widget

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.unit.dp
import com.example.domain.models.SaveStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuickNotesOverlay(
    onSave: suspend (String, (SaveStatus) -> Unit) -> Unit,
    onClose: () -> Unit,
    onScreenshot: () -> Unit,
    screenshot:Bitmap? = null
) {
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
//            var screenshot by remember { mutableStateOf<Bitmap?>(null) }
            saveStatus?.let {
                when (it) {
                    SaveStatus.SUCCESS -> Text("Saved!", color = Color.Green)
                    SaveStatus.ERROR -> Text("Error!", color = Color.Red)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Note",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    onScreenshot()
                }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Screenshot"
                    )
                }
            }
            screenshot?.let{
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(8.dp))
            var text by remember { mutableStateOf("") }
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Note") })
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onClose) { Text("Close") }
                Button(onClick = {
                    coroutineScope.launch {
                        onSave(text) {
                            saveStatus = it
                            if(it == SaveStatus.SUCCESS){
                                text = ""
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
}