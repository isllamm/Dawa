package com.family.dawa.presentation.admin.medications

import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.usecase.medications.ArchiveMedicationUseCase
import com.family.dawa.domain.usecase.medications.GetMedicationsUseCase
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminMedListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medications = MutableSharedFlow<List<Medication>>(replay = 1)
    private val getMedications: GetMedicationsUseCase = mockk()
    private val archiveMedication: ArchiveMedicationUseCase = mockk(relaxed = true)

    private val active = Medication(id = 1, name = "بانادول", active = true)
    private val archived = Medication(id = 2, name = "قديم", active = false)

    @Before
    fun setUp() {
        every { getMedications.getAll() } returns medications
    }

    private fun createViewModel() = AdminMedListViewModel(getMedications, archiveMedication)

    @Test
    fun testInit_listsAllMedicationsIncludingArchived() {
        val vm = createViewModel()
        assertTrue(vm.state.value.isLoading)

        medications.tryEmit(listOf(active, archived))

        assertEquals(AdminMedListState(medications = listOf(active, archived), isLoading = false), vm.state.value)
    }

    @Test
    fun testToggleArchive_activeMedicationIsArchived() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedListIntent.ToggleArchive(active))

        coVerify { archiveMedication(id = 1, activate = false) }
    }

    @Test
    fun testToggleArchive_archivedMedicationIsReactivated() {
        val vm = createViewModel()

        vm.sendIntent(AdminMedListIntent.ToggleArchive(archived))

        coVerify { archiveMedication(id = 2, activate = true) }
    }
}
