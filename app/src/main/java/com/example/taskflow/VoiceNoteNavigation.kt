package com.example.taskflow

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.feature.notes.home.HomeScreen
import com.example.feature.notes.noteList.NoteListScreen

@Composable
fun VoiceNoteNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToNotesList = {
                    navController.navigate("notes_list")
                }
            )
        }

        composable("notes_list") {
            NoteListScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}