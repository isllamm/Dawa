package com.family.dawa.presentation.admin.medications.editor

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.audio.AudioRecorder
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.image.ImageStore
import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.MedicationPhoto
import com.family.dawa.domain.model.Schedule
import com.family.dawa.domain.usecase.medications.GetMedicationByIdUseCase
import com.family.dawa.domain.usecase.medications.SaveMedicationUseCase
import kotlinx.coroutines.launch

class AdminMedEditorViewModel(
    private val getMedicationByIdUseCase: GetMedicationByIdUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val imageStore: ImageStore,
    private val audioRecorder: AudioRecorder,
    private val voicePlayer: VoicePlayer
) : MviViewModel<AdminMedEditorIntent, AdminMedEditorState, AdminMedEditorEffect>(AdminMedEditorState()) {

    override fun handleIntent(intent: AdminMedEditorIntent) {
        when (intent) {
            is AdminMedEditorIntent.Load -> {
                if (intent.id != 0L) {
                    viewModelScope.launch {
                        val med = getMedicationByIdUseCase(intent.id) ?: return@launch
                        val photos = getMedicationByIdUseCase.getPhotos(intent.id)
                        val schedules = getMedicationByIdUseCase.getSchedules(intent.id)
                        updateState {
                            copy(
                                medicationId = med.id,
                                name = med.name,
                                strength = med.strength,
                                notes = med.notes,
                                audioPath = med.audioPath,
                                photoPath = photos.firstOrNull()?.path,
                                schedules = if (schedules.isNotEmpty()) schedules else schedules
                            )
                        }
                    }
                }
            }
            is AdminMedEditorIntent.NameChanged -> updateState { copy(name = intent.name) }
            is AdminMedEditorIntent.StrengthChanged -> updateState { copy(strength = intent.strength) }
            is AdminMedEditorIntent.NotesChanged -> updateState { copy(notes = intent.notes) }
            is AdminMedEditorIntent.PhotoSelected -> updateState { copy(photoPath = intent.path) }
            is AdminMedEditorIntent.PhotoRemoved -> updateState { copy(photoPath = null) }
            is AdminMedEditorIntent.StartRecordingAudio -> {
                val recordedPath = audioRecorder.startRecording()
                updateState { copy(audioPath = recordedPath, isRecording = true) }
            }
            is AdminMedEditorIntent.StopRecordingAudio -> {
                val finalPath = audioRecorder.stopRecording()
                updateState { copy(audioPath = finalPath, isRecording = false) }
            }
            is AdminMedEditorIntent.PlayRecordedAudio -> {
                state.value.audioPath?.let { path ->
                    viewModelScope.launch { voicePlayer.playAudioFile(path) }
                }
            }
            is AdminMedEditorIntent.AddSchedule -> {
                updateState {
                    copy(schedules = schedules + Schedule(medicationId = medicationId, timeOfDayMinutes = 840, quantityHalves = 2))
                }
            }
            is AdminMedEditorIntent.UpdateSchedule -> {
                val updated = state.value.schedules.toMutableList()
                if (intent.index in updated.indices) {
                    updated[intent.index] = intent.schedule
                    updateState { copy(schedules = updated) }
                }
            }
            is AdminMedEditorIntent.RemoveSchedule -> {
                val updated = state.value.schedules.toMutableList()
                if (intent.index in updated.indices && updated.size > 1) {
                    updated.removeAt(intent.index)
                    updateState { copy(schedules = updated) }
                }
            }
            is AdminMedEditorIntent.SaveMedication -> {
                val current = state.value
                if (current.name.isBlank()) return
                viewModelScope.launch {
                    updateState { copy(isSaving = true) }
                    val med = Medication(
                        id = current.medicationId,
                        name = current.name.trim(),
                        strength = current.strength.trim(),
                        notes = current.notes.trim(),
                        audioPath = current.audioPath,
                        active = true
                    )
                    val photos = if (!current.photoPath.isNullOrEmpty()) {
                        listOf(MedicationPhoto(path = current.photoPath, isPrimary = true))
                    } else emptyList()

                    saveMedicationUseCase(med, photos, current.schedules)
                    emitEffect(AdminMedEditorEffect.NavigateBack)
                }
            }
        }
    }
}
