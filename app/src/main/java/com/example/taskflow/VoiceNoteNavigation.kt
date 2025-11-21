package com.example.taskflow

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature.notes.home.HomeScreen
import com.example.feature.notes.noteDetail.NoteDetailScreen
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
                },
                onItemClick = { id ->
                    navController.navigate("note_detail/$id")
                }
            )
        }

        composable("note_detail/{itemId}", arguments = listOf(navArgument("itemId"){type = NavType.LongType})) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId")
            NoteDetailScreen(
                itemId = itemId ?: -1
            )
        }
    }
}