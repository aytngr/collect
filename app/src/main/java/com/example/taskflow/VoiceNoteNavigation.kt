package com.example.taskflow

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        startDestination = "home",
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
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