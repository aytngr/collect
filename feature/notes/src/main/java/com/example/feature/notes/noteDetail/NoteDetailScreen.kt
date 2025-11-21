package com.example.feature.notes.noteDetail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController

@Composable
fun NoteDetailScreen(
    itemId: Long,
    viewModel: NoteDetailViewModel = hiltViewModel()
){
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ){
            state.note?.images?.let { images ->
                LazyRow (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    items(items = images){ image ->
                        Image(
                            bitmap = BitmapFactory.decodeFile(image).asImageBitmap(),
                            contentDescription = "Screenshot",
                            modifier = Modifier
                                .width(120.dp)
                                .wrapContentHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            Text(modifier = Modifier, text = state.note?.content ?: "")
        }
    }


}