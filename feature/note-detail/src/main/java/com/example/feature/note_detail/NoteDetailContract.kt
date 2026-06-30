package com.example.feature.note_detail

import androidx.compose.ui.text.input.TextFieldValue
import com.example.core.common.base.BaseEffect
import com.example.core.common.base.BaseIntent
import com.example.core.common.base.BaseState
import com.example.domain.models.Note

class NoteDetailContract {

    sealed class Intent: BaseIntent {
        data class LoadNote(val id: Long): Intent()
        data class UpdateNote(val title: String? = null, val content: String? = null, val images: List<String>? = null): Intent()
        data object SaveNow : Intent()
        data object PinNote : Intent()
        data object RefreshPermissionDialogVisibility : Intent()
        data object CheckExactAlarmPermission : Intent()
        data class DeleteNote(val note: Note): Intent()
        data class SetReminder(val at: Long): Intent()
        data class SetPendingReminder(val at: Long): Intent()
        data object SchedulePendingReminder : Intent()
        data object ClearReminder : Intent()
    }

    data class State(
        val note: Note,
        val isLoading: Boolean = false,
        val showExactAlarmPermissionDialog: Boolean = false,
        val createdTime: String = "",
        val reminder: String = "",
        val editedTime: String = "",
        val error: String? = null,
    ): BaseState

    sealed class Effect: BaseEffect {
        data object NavigateBack: Effect()
    }
}