package com.aytngr.feature.home

import com.aytngr.core.common.base.BaseEffect
import com.aytngr.core.common.base.BaseIntent
import com.aytngr.core.common.base.BaseState
import com.aytngr.domain.models.Note

class HomeContract {
    sealed class Intent : BaseIntent {
        object CreateNewNote : Intent()
        object RefreshOverlayStatus : Intent()
        object StartOverlay : Intent()
    }

    data class State(
        val upNextReminders: List<Note> = emptyList(),
        val transcribedText: String = "",
        val recentNotes: List<Note> = emptyList(),
        val timeAgo: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val showOverlayDialog: Boolean = false,
        val error: String? = null,
        val volumeLevel: Float = 0f,
        val date: String = "",
        val greeting: String = "",
        val reminderCount: Int = 0
    ) : BaseState

    sealed class Effect : BaseEffect {
        data class ShowToast(val message: String) : Effect()
        data class NavigateToNoteDetail(val id: Long) : Effect()
        data class ShowError(val error: String) : Effect()
    }
}