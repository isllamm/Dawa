package com.family.dawa.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

import com.family.dawa.domain.model.AppSettings
import com.family.dawa.domain.repository.ISettingsRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dawa_settings")

class SettingsRepository(private val context: Context) : ISettingsRepository {

    private object Keys {
        val PATIENT_NAME = stringPreferencesKey("patient_name")
        val CAREGIVER_NAME = stringPreferencesKey("caregiver_name")
        val GRACE_MINUTES = intPreferencesKey("grace_minutes")
        val REALERT_MINUTES = intPreferencesKey("realert_minutes")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        val DEMO_SEEDED = booleanPreferencesKey("demo_seeded")
        val DEBUG_TIME_OFFSET_MS = longPreferencesKey("debug_time_offset_ms")
        val BREAKFAST_MINUTES = intPreferencesKey("breakfast_minutes")
        val LUNCH_MINUTES = intPreferencesKey("lunch_minutes")
        val DINNER_MINUTES = intPreferencesKey("dinner_minutes")
        val SLEEP_MINUTES = intPreferencesKey("sleep_minutes")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            patientName = prefs[Keys.PATIENT_NAME] ?: "جدو",
            caregiverName = prefs[Keys.CAREGIVER_NAME] ?: "تيتا",
            graceMinutes = prefs[Keys.GRACE_MINUTES] ?: 60,
            realertMinutes = prefs[Keys.REALERT_MINUTES] ?: 5,
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            disclaimerAccepted = prefs[Keys.DISCLAIMER_ACCEPTED] ?: false,
            demoSeeded = prefs[Keys.DEMO_SEEDED] ?: false,
            debugTimeOffsetMs = prefs[Keys.DEBUG_TIME_OFFSET_MS] ?: 0L,
            breakfastMinutes = prefs[Keys.BREAKFAST_MINUTES] ?: 480,
            lunchMinutes = prefs[Keys.LUNCH_MINUTES] ?: 840,
            dinnerMinutes = prefs[Keys.DINNER_MINUTES] ?: 1200,
            sleepMinutes = prefs[Keys.SLEEP_MINUTES] ?: 1350,
            hasPin = !prefs[Keys.PIN_HASH].isNullOrEmpty()
        )
    }

    override suspend fun getSettings(): AppSettings = settingsFlow.first()

    override suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { it[Keys.DISCLAIMER_ACCEPTED] = accepted }
    }

    override suspend fun setDemoSeeded(seeded: Boolean) {
        context.dataStore.edit { it[Keys.DEMO_SEEDED] = seeded }
    }

    override suspend fun setDebugTimeOffset(offsetMs: Long) {
        context.dataStore.edit { it[Keys.DEBUG_TIME_OFFSET_MS] = offsetMs }
    }

    override suspend fun updateSettings(
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
        context.dataStore.edit { prefs ->
            prefs[Keys.PATIENT_NAME] = patientName
            prefs[Keys.CAREGIVER_NAME] = caregiverName
            prefs[Keys.GRACE_MINUTES] = graceMinutes
            prefs[Keys.REALERT_MINUTES] = realertMinutes
            prefs[Keys.VOICE_ENABLED] = voiceEnabled
            prefs[Keys.VIBRATION_ENABLED] = vibrationEnabled
            prefs[Keys.BREAKFAST_MINUTES] = breakfastMinutes
            prefs[Keys.LUNCH_MINUTES] = lunchMinutes
            prefs[Keys.DINNER_MINUTES] = dinnerMinutes
            prefs[Keys.SLEEP_MINUTES] = sleepMinutes
        }
    }

    override suspend fun setAdminPin(pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.getEncoder().encodeToString(saltBytes)
        val hash = hashPin(pin, salt)

        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = salt
            prefs[Keys.PIN_HASH] = hash
        }
    }

    override suspend fun verifyAdminPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val savedHash = prefs[Keys.PIN_HASH] ?: return (pin == "1234") // default demo PIN
        val savedSalt = prefs[Keys.PIN_SALT] ?: return (pin == "1234")
        return hashPin(pin, savedSalt) == savedHash
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val digest = md.digest(pin.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }
}
