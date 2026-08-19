package com.aytngr.collect

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aytngr.core.common.base.permissions.PermissionHandler
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.CollectTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionHandler: PermissionHandler

    val newIntents = Channel<Intent>(Channel.UNLIMITED)

    val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* denied → reminders won't show; surface it once */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colors.bg
                ) {
                    val navController = rememberNavController()
                    LaunchedEffect(Unit) {
                        if(!permissionHandler.hasNotificationPermission()) notifPermission.launch(
                            Manifest.permission.POST_NOTIFICATIONS)
                        newIntents.receiveAsFlow().collect { navController.handleDeepLink(it) }
                    }
                    VoiceNoteNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        newIntents.trySend(intent)
    }
}
