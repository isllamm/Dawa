package com.family.dawa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.family.dawa.BuildConfig
import com.family.dawa.presentation.admin.contact.AdminContactScreen
import com.family.dawa.presentation.admin.dashboard.AdminDashboardScreen
import com.family.dawa.presentation.admin.debug.DebugScreen
import com.family.dawa.presentation.admin.health.AdminHealthCheckScreen
import com.family.dawa.presentation.admin.history.AdminHistoryScreen
import com.family.dawa.presentation.admin.medications.AdminMedListScreen
import com.family.dawa.presentation.admin.medications.editor.AdminMedEditorScreen
import com.family.dawa.presentation.admin.pin.AdminPinScreen
import com.family.dawa.presentation.admin.settings.AdminSettingsScreen
import com.family.dawa.presentation.caregiver.CaregiverScreen

@Composable
fun DawaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CAREGIVER,
        modifier = modifier
    ) {
        // 1. Caregiver Screen (Default, single screen for Grandma)
        composable(Routes.CAREGIVER) {
            CaregiverScreen(
                onOpenAdminPin = { navController.navigate(Routes.ADMIN_PIN) }
            )
        }

        // 2. Admin PIN Gate
        composable(Routes.ADMIN_PIN) {
            AdminPinScreen(
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
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Admin History
        composable(Routes.ADMIN_HISTORY) {
            AdminHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Admin Contact
        composable(Routes.ADMIN_CONTACT) {
            AdminContactScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Admin Settings
        composable(Routes.ADMIN_SETTINGS) {
            AdminSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 9. Admin Health Check
        composable(Routes.ADMIN_HEALTH) {
            AdminHealthCheckScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 10. Admin Developer & Debug (debug builds only)
        if (BuildConfig.DEBUG) {
            composable(Routes.ADMIN_DEBUG) {
                DebugScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
