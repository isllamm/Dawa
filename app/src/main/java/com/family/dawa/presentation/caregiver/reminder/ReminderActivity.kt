package com.family.dawa.presentation.caregiver.reminder

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.family.dawa.domain.model.HomeState
import com.family.dawa.presentation.caregiver.CaregiverScreen
import com.family.dawa.presentation.caregiver.CaregiverViewModel
import com.family.dawa.ui.theme.DawaTheme
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderActivity : ComponentActivity() {

    private val viewModel: CaregiverViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and display over keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            DawaTheme {
                val uiState by viewModel.state.collectAsState()

                // Auto-close ReminderActivity after dose is confirmed and DONE overlay finishes
                LaunchedEffect(uiState.homeState, uiState.doneOverlaySlot) {
                    if (uiState.doneOverlaySlot == null && uiState.homeState !is HomeState.Due) {
                        delay(500)
                        finish()
                    }
                }

                CaregiverScreen(
                    viewModel = viewModel,
                    onOpenAdminPin = { finish() }
                )
            }
        }
    }
}
