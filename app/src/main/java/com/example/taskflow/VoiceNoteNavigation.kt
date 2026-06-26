package com.example.taskflow

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.core.navigation.Home
import com.example.core.navigation.NoteDetail
import com.example.core.navigation.Notes
import com.example.feature.home.HomeScreen
import com.example.feature.note_detail.NoteDetailScreen
import com.example.feature.notes.noteList.NoteListScreen
import com.example.feature.overlay.OverlayService

@Composable
fun VoiceNoteNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        composable<Home> {
            HomeScreen(
                onNavigateToNotesList = {
                    navController.navigate(Notes)
                },
                onNavigateToNoteDetail = { id -> navController.navigate(NoteDetail(id)) },
            )
        }

        composable<Notes> {
            NoteListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemClick = { id ->
                    navController.navigate(NoteDetail(id))
                }
            )
        }

        composable<NoteDetail> { backStackEntry ->
            val route: NoteDetail = backStackEntry.toRoute()
            NoteDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                itemId = route.noteId
            )
        }
    }
}