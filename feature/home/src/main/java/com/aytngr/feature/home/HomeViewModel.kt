package com.aytngr.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.aytngr.core.common.base.BaseViewModel
import com.aytngr.core.common.base.permissions.PermissionHandler
import com.aytngr.domain.models.onError
import com.aytngr.domain.models.onSuccess
import com.aytngr.domain.overlay.OverlayController
import com.aytngr.domain.usecase.CreateEmptyNoteUseCase
import com.aytngr.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val createEmptyNoteUseCase: CreateEmptyNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val permissionHandler: PermissionHandler,
    private val overlay: OverlayController,
) : BaseViewModel<HomeContract.Intent, HomeContract.State, HomeContract.Effect>(HomeContract.State()) {

    private var voiceRecognitionJob: Job? = null


    init {
        setState {
            HomeContract.State(
                greetingRes = greetingRes(),
                showOverlayDialog = !overlay.isRunning()
            )
        }
        loadRecentNotes()
    }

    override fun handleIntent(intent: HomeContract.Intent) {
        when (intent) {
            is HomeContract.Intent.CreateNewNote -> createNewNote()
            is HomeContract.Intent.RefreshOverlayStatus -> setState { copy(showOverlayDialog = intent.show) }
            HomeContract.Intent.StartOverlay -> {
                overlay.start()
                setState { copy(showOverlayDialog = false) }
            }
        }
    }

    private fun createNewNote() {
        viewModelScope.launch {
            createEmptyNoteUseCase()
                .onSuccess { note ->
                    sendEffect(HomeContract.Effect.NavigateToNoteDetail(note.id))
                }
                .onError { error ->
                    sendEffect(HomeContract.Effect.ShowError(error.message ?: ""))
                }
        }
    }

    private fun loadRecentNotes() {
        viewModelScope.launch {
            getNotesUseCase.invoke()
                .collect { notes ->
                    notes.onSuccess { notes ->
                        val recentNotes = notes.take(3)
                        val upNextReminders = notes.filter { it.reminderAt != null }
                            .filter { it.reminderAt!! > System.currentTimeMillis() }
                        setState {
                            copy(
                                upNextReminders = upNextReminders.take(2),
                                reminderCount = upNextReminders.size,
                                recentNotes = recentNotes)
                        }
                    }
                        .onError {

                        }
                }
        }

    }

    @StringRes
    private fun greetingRes(): Int = when (LocalTime.now().hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        in 18..21 -> R.string.home_greeting_evening
        else -> R.string.home_greeting_night
    }

}