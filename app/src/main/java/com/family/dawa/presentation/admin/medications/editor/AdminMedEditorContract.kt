package com.family.dawa.presentation.admin.medications.editor

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.Schedule

sealed interface AdminMedEditorIntent : ViewIntent {
    data class Load(val id: Long) : AdminMedEditorIntent
    data class NameChanged(val name: String) : AdminMedEditorIntent
    data class StrengthChanged(val strength: String) : AdminMedEditorIntent
    data class NotesChanged(val notes: String) : AdminMedEditorIntent
    data class PhotoSelected(val path: String) : AdminMedEditorIntent
    data object PhotoRemoved : AdminMedEditorIntent
    data object StartRecordingAudio : AdminMedEditorIntent
    data object StopRecordingAudio : AdminMedEditorIntent
    data object PlayRecordedAudio : AdminMedEditorIntent
    data object AddSchedule : AdminMedEditorIntent
    data class UpdateSchedule(val index: Int, val schedule: Schedule) : AdminMedEditorIntent
    data class RemoveSchedule(val index: Int) : AdminMedEditorIntent
    data object SaveMedication : AdminMedEditorIntent
}

data class AdminMedEditorState(
    val medicationId: Long = 0L,
    val name: String = "",
    val strength: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val audioPath: String? = null,
    val schedules: List<Schedule> = listOf(Schedule(timeOfDayMinutes = 480, quantityHalves = 2)),
    val isRecording: Boolean = false,
    val isSaving: Boolean = false
) : ViewState

sealed interface AdminMedEditorEffect : ViewEffect {
    data object NavigateBack : AdminMedEditorEffect
}
