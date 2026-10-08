package com.family.dawa.domain.model

data class AppSettings(
    val patientName: String = "جدو",
    val caregiverName: String = "تيتا",
    val graceMinutes: Int = 60,
    val realertMinutes: Int = 5,
    val voiceEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val disclaimerAccepted: Boolean = false,
    val demoSeeded: Boolean = false,
    val debugTimeOffsetMs: Long = 0L,
    val breakfastMinutes: Int = 480, // 8:00 AM
    val lunchMinutes: Int = 840,     // 2:00 PM
    val dinnerMinutes: Int = 1200,   // 8:00 PM
    val sleepMinutes: Int = 1350,    // 10:30 PM
    val hasPin: Boolean = false
)
