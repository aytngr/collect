package com.example.feature.notes.noteDetail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController

@Composable
fun NoteDetailScreen(
    itemId: Long,
    viewModel: NoteDetailViewModel = hiltViewModel()
){
    val navController = rememberNavController()
    var showImagePreview by remember { mutableIntStateOf(-1) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    var content by remember { mutableStateOf(state.note?.content) }
    var title by remember { mutableStateOf(state.note?.title) }

    LaunchedEffect(viewModel) {
        viewModel.handleIntent(NoteDetailContract.Intent.LoadNote(itemId))
        viewModel.effects.collect { effect ->
            when(effect){
                NoteDetailContract.Effect.NavigateBack -> navController.popBackStack()
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }else{
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                value = (if(title == null) state.note?.title else title) ?: "",
                onValueChange = {
                    title = it
                    viewModel.handleIntent(
                        NoteDetailContract.Intent.ChangeText(
                            note = state.note!!,
                            title = it
                        )
                    )
                }
            )
            state.note?.images?.let { images ->
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    itemsIndexed(items = images){ index, image ->
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
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                value = (if(content == null) state.note?.content else content) ?: "",
                onValueChange = {
                    content = it
                    viewModel.handleIntent(
                        NoteDetailContract.Intent.ChangeText(
                            note = state.note!!,
                            content = it
                        )
                    )
                }
            )
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
                        bitmap = BitmapFactory.decodeFile(state.note?.images!![showImagePreview]).asImageBitmap(),
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