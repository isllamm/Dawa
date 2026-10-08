package com.family.dawa.di

import android.content.Context
import com.family.dawa.alarm.AlarmSync
import com.family.dawa.alarm.NotificationHelper
import com.family.dawa.core.audio.AudioRecorder
import com.family.dawa.core.audio.TtsEngine
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.image.ImageStore
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.core.time.SystemTimeProvider
import com.family.dawa.data.db.DawaDatabase
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.data.repo.ContactRepository
import com.family.dawa.data.repo.DoseRepository
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.ledger.DoseLedger

class AppContainer(val context: Context) {

    val database = DawaDatabase.getInstance(context)

    val medicationDao = database.medicationDao()
    val medicationPhotoDao = database.medicationPhotoDao()
    val scheduleDao = database.scheduleDao()
    val doseEventDao = database.doseEventDao()
    val contactDao = database.contactDao()

    val settingsRepository = SettingsRepository(context)
    val timeProvider = DebugTimeProvider(SystemTimeProvider())

    val medicationRepository = MedicationRepository(
        medicationDao = medicationDao,
        photoDao = medicationPhotoDao,
        scheduleDao = scheduleDao
    )

    val contactRepository = ContactRepository(contactDao)

    val doseLedger = DoseLedger(
        doseEventDao = doseEventDao,
        medicationRepository = medicationRepository
    )

    val doseRepository = DoseRepository(
        medicationRepository = medicationRepository,
        doseEventDao = doseEventDao,
        contactRepository = contactRepository,
        settingsRepository = settingsRepository,
        timeProvider = timeProvider,
        doseLedger = doseLedger
    )

    val imageStore = ImageStore(context)
    val audioRecorder = AudioRecorder(context)
    val ttsEngine = TtsEngine(context).apply { init() }
    val voicePlayer = VoicePlayer(context, ttsEngine)

    val notificationHelper = NotificationHelper(context).apply {
        createNotificationChannels()
    }

    val alarmSync = AlarmSync(
        context = context,
        medicationRepository = medicationRepository,
        doseEventDao = doseEventDao,
        settingsRepository = settingsRepository,
        timeProvider = timeProvider
    )

    val demoSeeder = DemoSeeder(
        context = context,
        medicationRepository = medicationRepository,
        contactRepository = contactRepository,
        settingsRepository = settingsRepository
    )
}
