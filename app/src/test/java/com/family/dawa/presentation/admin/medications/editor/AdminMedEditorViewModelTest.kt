package com.family.dawa.presentation.admin.medications.editor

import app.cash.turbine.test
import com.family.dawa.core.audio.AudioRecorder
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.image.ImageStore
import com.family.dawa.domain.model.MealRelation
import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.MedicationPhoto
import com.family.dawa.domain.model.Schedule
import com.family.dawa.domain.usecase.medications.GetMedicationByIdUseCase
import com.family.dawa.domain.usecase.medications.SaveMedicationUseCase
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminMedEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMedicationById: GetMedicationByIdUseCase = mockk()
    private val saveMedication: SaveMedicationUseCase = mockk()
    private val imageStore: ImageStore = mockk(relaxed = true)
    private val audioRecorder: AudioRecorder = mockk()
    private val voicePlayer: VoicePlayer = mockk(relaxed = true)

    private val defaultSchedule = Schedule(timeOfDayMinutes = 480, quantityHalves = 2)

    private val storedMed = Medication(
        id = 5,
        name = "بانادول",
        strength = "500mg",
        notes = "بعد الأكل",
        audioPath = "/audio/5.m4a",
        colorTag = 0xFF2B78E4.toInt(),
        shapeTag = "OVAL",
        active = false,
        createdAt = 123L
    )
    private val storedSchedules = listOf(
        Schedule(id = 1, medicationId = 5, timeOfDayMinutes = 480, mealRelation = MealRelation.AFTER_BREAKFAST),
        Schedule(id = 2, medicationId = 5, timeOfDayMinutes = 1200, quantityHalves = 1)
    )
    private val storedPhotos = listOf(
        MedicationPhoto(id = 1, medicationId = 5, path = "/box.jpg", isPrimary = true),
        MedicationPhoto(id = 2, medicationId = 5, path = "/pill.jpg")
    )

    @Before
    fun setUp() {
        coEvery { getMedicationById(5) } returns storedMed
        coEvery { getMedicationById.getPhotos(5) } returns storedPhotos
        coEvery { getMedicationById.getSchedules(5) } returns storedSchedules
        coEvery { saveMedication(any(), any(), any()) } returns 5L
    }

    private fun createViewModel() =
        AdminMedEditorViewModel(getMedicationById, saveMedication, imageStore, audioRecorder, voicePlayer)

    private fun loadedViewModel() = createViewModel().apply { sendIntent(AdminMedEditorIntent.Load(5)) }

    // region load

    @Test
    fun testInitialState_hasOneDefaultSchedule() {
        val vm = createViewModel()

        assertEquals(listOf(defaultSchedule), vm.state.value.schedules)
        assertEquals(0L, vm.state.value.medicationId)
    }

    @Test
    fun testLoad_zeroIdIsNewMedicationAndDoesNotQuery() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.Load(0))

        assertEquals(AdminMedEditorState(), vm.state.value)
        coVerify(exactly = 0) { getMedicationById(any()) }
    }

    @Test
    fun testLoad_fillsStateFromStoredMedication() {
        val vm = loadedViewModel()

        val state = vm.state.value
        assertEquals(storedMed, state.loadedMedication)
        assertEquals(5L, state.medicationId)
        assertEquals("بانادول", state.name)
        assertEquals("500mg", state.strength)
        assertEquals("بعد الأكل", state.notes)
        assertEquals("/audio/5.m4a", state.audioPath)
        assertEquals("/box.jpg", state.photoPath)
        assertEquals(storedSchedules, state.schedules)
    }

    @Test
    fun testLoad_unknownMedicationLeavesStateUnchanged() {
        coEvery { getMedicationById(9) } returns null
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.Load(9))

        assertEquals(AdminMedEditorState(), vm.state.value)
    }

    @Test
    fun testLoad_medicationWithoutSchedulesKeepsDefaultSchedule() {
        coEvery { getMedicationById.getSchedules(5) } returns emptyList()

        val vm = loadedViewModel()

        assertEquals(listOf(defaultSchedule), vm.state.value.schedules)
    }

    // endregion

    // region simple fields

    @Test
    fun testTextChanges_updateState() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.NameChanged("كونكور"))
        vm.sendIntent(AdminMedEditorIntent.StrengthChanged("5mg"))
        vm.sendIntent(AdminMedEditorIntent.NotesChanged("الصبح"))

        assertEquals("كونكور", vm.state.value.name)
        assertEquals("5mg", vm.state.value.strength)
        assertEquals("الصبح", vm.state.value.notes)
    }

    @Test
    fun testPhotoSelectedThenRemoved() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.PhotoSelected("/new.jpg"))
        assertEquals("/new.jpg", vm.state.value.photoPath)

        vm.sendIntent(AdminMedEditorIntent.PhotoRemoved)
        assertNull(vm.state.value.photoPath)
    }

    // endregion

    // region audio

    @Test
    fun testRecording_startAndStopUpdatePathAndFlag() {
        every { audioRecorder.startRecording() } returns "/rec.m4a"
        every { audioRecorder.stopRecording() } returns "/rec.m4a"
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.StartRecordingAudio)
        assertTrue(vm.state.value.isRecording)
        assertEquals("/rec.m4a", vm.state.value.audioPath)

        vm.sendIntent(AdminMedEditorIntent.StopRecordingAudio)
        assertFalse(vm.state.value.isRecording)
        assertEquals("/rec.m4a", vm.state.value.audioPath)
    }

    @Test
    fun testStopRecording_failureClearsAudioPath() {
        every { audioRecorder.startRecording() } returns "/rec.m4a"
        every { audioRecorder.stopRecording() } returns null
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.StartRecordingAudio)
        vm.sendIntent(AdminMedEditorIntent.StopRecordingAudio)

        assertNull(vm.state.value.audioPath)
        assertFalse(vm.state.value.isRecording)
    }

    @Test
    fun testPlayRecordedAudio_playsCurrentFile() {
        val vm = loadedViewModel()

        vm.sendIntent(AdminMedEditorIntent.PlayRecordedAudio)

        coVerify { voicePlayer.playAudioFile("/audio/5.m4a") }
    }

    @Test
    fun testPlayRecordedAudio_withoutAudioDoesNothing() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.PlayRecordedAudio)

        coVerify(exactly = 0) { voicePlayer.playAudioFile(any()) }
    }

    // endregion

    // region schedules

    @Test
    fun testAddSchedule_adds2pmScheduleForThisMedication() {
        val vm = loadedViewModel()

        vm.sendIntent(AdminMedEditorIntent.AddSchedule)

        assertEquals(
            storedSchedules + Schedule(medicationId = 5, timeOfDayMinutes = 840, quantityHalves = 2),
            vm.state.value.schedules
        )
    }

    @Test
    fun testUpdateSchedule_replacesScheduleAtIndex() {
        val vm = loadedViewModel()
        val changed = storedSchedules[1].copy(timeOfDayMinutes = 1260)

        vm.sendIntent(AdminMedEditorIntent.UpdateSchedule(1, changed))

        assertEquals(listOf(storedSchedules[0], changed), vm.state.value.schedules)
    }

    @Test
    fun testUpdateSchedule_invalidIndexIsIgnored() {
        val vm = loadedViewModel()

        vm.sendIntent(AdminMedEditorIntent.UpdateSchedule(5, defaultSchedule))
        vm.sendIntent(AdminMedEditorIntent.UpdateSchedule(-1, defaultSchedule))

        assertEquals(storedSchedules, vm.state.value.schedules)
    }

    @Test
    fun testRemoveSchedule_removesScheduleAtIndex() {
        val vm = loadedViewModel()

        vm.sendIntent(AdminMedEditorIntent.RemoveSchedule(0))

        assertEquals(listOf(storedSchedules[1]), vm.state.value.schedules)
    }

    @Test
    fun testRemoveSchedule_lastScheduleCannotBeRemoved() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedEditorIntent.RemoveSchedule(0))

        assertEquals(listOf(defaultSchedule), vm.state.value.schedules)
    }

    @Test
    fun testRemoveSchedule_invalidIndexIsIgnored() {
        val vm = loadedViewModel()

        vm.sendIntent(AdminMedEditorIntent.RemoveSchedule(7))

        assertEquals(storedSchedules, vm.state.value.schedules)
    }

    // endregion

    // region save

    @Test
    fun testSave_blankNameDoesNothing() = runTest {
        val vm = createViewModel()
        vm.sendIntent(AdminMedEditorIntent.NameChanged("   "))

        vm.effect.test {
            vm.sendIntent(AdminMedEditorIntent.SaveMedication)
            expectNoEvents()
        }
        coVerify(exactly = 0) { saveMedication(any(), any(), any()) }
        assertFalse(vm.state.value.isSaving)
    }

    @Test
    fun testSave_newMedicationTrimsFieldsSavesPhotoAndNavigatesBack() = runTest {
        val vm = createViewModel()
        vm.sendIntent(AdminMedEditorIntent.NameChanged("  كونكور "))
        vm.sendIntent(AdminMedEditorIntent.StrengthChanged(" 5mg "))
        vm.sendIntent(AdminMedEditorIntent.NotesChanged(" الصبح "))
        vm.sendIntent(AdminMedEditorIntent.PhotoSelected("/new.jpg"))
        val savedMed = slot<Medication>()
        val savedPhotos = slot<List<MedicationPhoto>>()
        val savedSchedules = slot<List<Schedule>>()
        coEvery { saveMedication(capture(savedMed), capture(savedPhotos), capture(savedSchedules)) } returns 9L

        vm.effect.test {
            vm.sendIntent(AdminMedEditorIntent.SaveMedication)
            assertEquals(AdminMedEditorEffect.NavigateBack, awaitItem())
        }

        with(savedMed.captured) {
            assertEquals(0L, id)
            assertEquals("كونكور", name)
            assertEquals("5mg", strength)
            assertEquals("الصبح", notes)
            assertTrue(active)
        }
        assertEquals(listOf(MedicationPhoto(path = "/new.jpg", isPrimary = true)), savedPhotos.captured)
        assertEquals(listOf(defaultSchedule), savedSchedules.captured)
        assertTrue(vm.state.value.isSaving)
    }

    @Test
    fun testSave_withoutPhotoSavesNoPhotos() {
        val vm = createViewModel()
        vm.sendIntent(AdminMedEditorIntent.NameChanged("كونكور"))

        vm.sendIntent(AdminMedEditorIntent.SaveMedication)

        coVerify { saveMedication(any(), emptyList(), any()) }
    }

    @Test
    fun testSave_editKeepsColourShapeAndCreationDate() {
        val vm = loadedViewModel()
        vm.sendIntent(AdminMedEditorIntent.NameChanged("بانادول اكسترا"))
        val savedMed = slot<Medication>()
        coEvery { saveMedication(capture(savedMed), any(), any()) } returns 5L

        vm.sendIntent(AdminMedEditorIntent.SaveMedication)

        assertEquals(
            storedMed.copy(name = "بانادول اكسترا", active = true),
            savedMed.captured
        )
    }

    // endregion
}
