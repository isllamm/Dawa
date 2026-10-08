package com.family.dawa

import android.app.Application
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.di.*
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DawaApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DawaApp)
            modules(
                appModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }

        val settingsRepository: SettingsRepository by inject()
        val timeProvider: DebugTimeProvider by inject()
        val demoSeeder: DemoSeeder by inject()
        val alarmScheduler: IAlarmScheduler by inject()

        applicationScope.launch {
            // Debug tools only: release builds never shift the clock or add demo medicines,
            // even if an offset was saved earlier by a debug build.
            if (BuildConfig.DEBUG) {
                val settings = settingsRepository.getSettings()
                timeProvider.offsetMillis = settings.debugTimeOffsetMs
                demoSeeder.seedIfNeeded()
            }
            alarmScheduler.resync()
        }
    }
}
