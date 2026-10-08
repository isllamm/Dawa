package com.family.dawa.di

import com.family.dawa.alarm.AlarmSync
import com.family.dawa.alarm.AndroidAlarmScheduler
import com.family.dawa.alarm.NotificationHelper
import com.family.dawa.core.audio.AudioRecorder
import com.family.dawa.core.audio.TtsEngine
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.image.ImageStore
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.core.time.SystemTimeProvider
import com.family.dawa.core.time.TimeProvider
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.domain.scheduler.IAlarmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { DebugTimeProvider(SystemTimeProvider()) }
    single<TimeProvider> { get<DebugTimeProvider>() }

    single { ImageStore(androidContext()) }
    single { AudioRecorder(androidContext()) }
    single { TtsEngine(androidContext()).apply { init() } }
    single { VoicePlayer(androidContext(), get()) }

    single { NotificationHelper(androidContext()).apply { createNotificationChannels() } }
    single { AlarmSync(androidContext(), get(), get(), get(), get()) }
    single<IAlarmScheduler> { AndroidAlarmScheduler(androidContext(), get()) }

    single { DemoSeeder(androidContext(), get(), get(), get()) }
}
