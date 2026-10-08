package com.family.dawa.domain.usecase.settings

import com.family.dawa.domain.model.AppSettings
import com.family.dawa.domain.repository.ISettingsRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val settingsRepository: ISettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.settingsFlow
    suspend fun current(): AppSettings = settingsRepository.getSettings()
}

class UpdateSettingsUseCase(
    private val settingsRepository: ISettingsRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(
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
    ) {
        settingsRepository.updateSettings(
            patientName = patientName,
            caregiverName = caregiverName,
            graceMinutes = graceMinutes,
            realertMinutes = realertMinutes,
            voiceEnabled = voiceEnabled,
            vibrationEnabled = vibrationEnabled,
            breakfastMinutes = breakfastMinutes,
            lunchMinutes = lunchMinutes,
            dinnerMinutes = dinnerMinutes,
            sleepMinutes = sleepMinutes
        )
        alarmScheduler.resync()
    }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        settingsRepository.setDisclaimerAccepted(accepted)
    }
}

class VerifyAdminPinUseCase(
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(pin: String): Boolean = settingsRepository.verifyAdminPin(pin)
}

class UpdateAdminPinUseCase(
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(pin: String) {
        settingsRepository.setAdminPin(pin)
    }
}
