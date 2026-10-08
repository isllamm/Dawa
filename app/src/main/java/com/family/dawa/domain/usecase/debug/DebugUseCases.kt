package com.family.dawa.domain.usecase.debug

import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.domain.repository.ISettingsRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler

class SetDebugTimeOffsetUseCase(
    private val timeProvider: DebugTimeProvider,
    private val settingsRepository: ISettingsRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(offsetMillis: Long) {
        timeProvider.offsetMillis = offsetMillis
        settingsRepository.setDebugTimeOffset(offsetMillis)
        alarmScheduler.resync()
    }
}

class ResetTodayEventsUseCase(
    private val doseRepository: IDoseRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke() {
        doseRepository.resetTodayEvents()
        alarmScheduler.resync()
    }
}

class ReseedDemoDataUseCase(
    private val demoSeeder: DemoSeeder,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke() {
        demoSeeder.forceSeed()
        alarmScheduler.resync()
    }
}
