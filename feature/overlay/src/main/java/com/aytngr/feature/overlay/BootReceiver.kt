package com.aytngr.feature.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.repository.PreferenceRepository
import com.aytngr.domain.scheduler.ReminderScheduler
import com.aytngr.domain.usecase.GetIsWidgetActiveUseCase
import com.aytngr.domain.usecase.GetNotesUseCase
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
    @Inject lateinit var getIsWidgetActiveUseCase: GetIsWidgetActiveUseCase


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (getIsWidgetActiveUseCase() && Settings.canDrawOverlays(context)) {
                        val serviceIntent = Intent(context, OverlayService::class.java)
                        context.startForegroundService(serviceIntent)
                    }
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