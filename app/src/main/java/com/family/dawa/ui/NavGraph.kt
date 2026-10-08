package com.family.dawa.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.family.dawa.di.AppContainer
import com.family.dawa.ui.admin.*
import com.family.dawa.ui.caregiver.CaregiverScreen
import com.family.dawa.ui.caregiver.CaregiverViewModel
import com.family.dawa.ui.debug.DebugScreen

object Routes {
    const val CAREGIVER = "caregiver"
    const val ADMIN_PIN = "admin_pin"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_MEDS = "admin_meds"
    const val ADMIN_MED_EDITOR = "admin_med_editor/{id}"
    const val ADMIN_HISTORY = "admin_history"
    const val ADMIN_CONTACT = "admin_contact"
    const val ADMIN_SETTINGS = "admin_settings"
    const val ADMIN_HEALTH = "admin_health"
    const val ADMIN_DEBUG = "admin_debug"

    fun medEditor(id: Long) = "admin_med_editor/$id"
}

@Composable
fun DawaNavGraph(
    navController: NavHostController,
    container: AppContainer,
    caregiverViewModel: CaregiverViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CAREGIVER
    ) {
        // 1. Caregiver Screen (Default, single screen for Grandma)
        composable(Routes.CAREGIVER) {
            CaregiverScreen(
                viewModel = caregiverViewModel,
                onOpenAdminPin = { navController.navigate(Routes.ADMIN_PIN) }
            )
        }

        // 2. Admin PIN Gate
        composable(Routes.ADMIN_PIN) {
            AdminPinScreen(
                settingsRepository = container.settingsRepository,
                onPinSuccess = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.CAREGIVER)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        // 3. Admin Dashboard
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                doseRepository = container.doseRepository,
                onNavigateMeds = { navController.navigate(Routes.ADMIN_MEDS) },
                onNavigateHistory = { navController.navigate(Routes.ADMIN_HISTORY) },
                onNavigateContact = { navController.navigate(Routes.ADMIN_CONTACT) },
                onNavigateSettings = { navController.navigate(Routes.ADMIN_SETTINGS) },
                onNavigateHealthCheck = { navController.navigate(Routes.ADMIN_HEALTH) },
                onNavigateDebug = { navController.navigate(Routes.ADMIN_DEBUG) },
                onExitAdmin = {
                    navController.navigate(Routes.CAREGIVER) {
                        popUpTo(Routes.CAREGIVER) { inclusive = true }
                    }
                }
            )
        }

        // 4. Admin Med List
        composable(Routes.ADMIN_MEDS) {
            AdminMedListScreen(
                medicationRepository = container.medicationRepository,
                onAddMedication = { navController.navigate(Routes.medEditor(0L)) },
                onEditMedication = { id -> navController.navigate(Routes.medEditor(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. Admin Med Editor
        composable(
            route = Routes.ADMIN_MED_EDITOR,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            AdminMedEditorScreen(
                medicationId = id,
                medicationRepository = container.medicationRepository,
                imageStore = container.imageStore,
                audioRecorder = container.audioRecorder,
                voicePlayer = container.voicePlayer,
                alarmSync = container.alarmSync,
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Admin History
        composable(Routes.ADMIN_HISTORY) {
            AdminHistoryScreen(
                doseRepository = container.doseRepository,
                timeProvider = container.timeProvider,
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Admin Contact
        composable(Routes.ADMIN_CONTACT) {
            AdminContactScreen(
                contactRepository = container.contactRepository,
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Admin Settings
        composable(Routes.ADMIN_SETTINGS) {
            AdminSettingsScreen(
                settingsRepository = container.settingsRepository,
                demoSeeder = container.demoSeeder,
                onBack = { navController.popBackStack() }
            )
        }

        // 9. Admin Health Check
        composable(Routes.ADMIN_HEALTH) {
            AdminHealthCheckScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 10. Admin Developer & Debug
        composable(Routes.ADMIN_DEBUG) {
            DebugScreen(
                timeProvider = container.timeProvider,
                settingsRepository = container.settingsRepository,
                doseRepository = container.doseRepository,
                alarmSync = container.alarmSync,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
