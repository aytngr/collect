package com.aytngr.feature.overlay

import android.app.AlarmManager
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
class ExactAlarmPermissionReceiver: BroadcastReceiver() {

    @Inject lateinit var getNotesUseCase: GetNotesUseCase
    @Inject lateinit var reminderScheduler: ReminderScheduler


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    getNotesUseCase().first().onSuccess { notes ->
                        reminderScheduler.scheduleReminders(notes)
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}