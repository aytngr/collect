package com.example.feature.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.models.onSuccess
import com.example.domain.scheduler.ReminderScheduler
import com.example.domain.usecase.GetNotesUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver: BroadcastReceiver() {

    @Inject lateinit var getNotesUseCase: GetNotesUseCase
    @Inject lateinit var reminderScheduler: ReminderScheduler


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            val serviceIntent = Intent(context, OverlayService::class.java)

            context.startForegroundService(serviceIntent)

            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val now = System.currentTimeMillis()
                    getNotesUseCase().first().onSuccess { notes ->
                        notes.filter { it.reminderAt != null && it.reminderAt!! > now }
                            .forEach { reminderScheduler.schedule(it.id, it.title, it.reminderAt!!) }
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}