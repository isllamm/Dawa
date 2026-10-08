package com.family.dawa

import android.app.Application
import com.family.dawa.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DawaApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        applicationScope.launch {
            // Restore debug offset if set
            val settings = container.settingsRepository.getSettings()
            container.timeProvider.offsetMillis = settings.debugTimeOffsetMs

            // Seed demo data on first install
            container.demoSeeder.seedIfNeeded()

            // Synchronize alarms
            container.alarmSync.resync()
        }
    }
}
