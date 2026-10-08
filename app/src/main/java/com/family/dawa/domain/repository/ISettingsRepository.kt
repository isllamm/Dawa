package com.family.dawa.domain.repository

import com.family.dawa.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun setDisclaimerAccepted(accepted: Boolean)
    suspend fun setDemoSeeded(seeded: Boolean)
    suspend fun setDebugTimeOffset(offsetMs: Long)
    suspend fun updateSettings(
        patientName: String,
        caregiverName: String,
        graceMinutes: Int,
        realertMinutes: Int,
        voiceEnabled: Boolean,
        vibrationEnabled: Boolean,
        breakfastMinutes: Int,
        lunchMinutes: Int,
        dinnerMinutes: Int,
        sleepMinutes: Int
    )
    suspend fun setAdminPin(pin: String)
    suspend fun verifyAdminPin(pin: String): Boolean
}
