package com.family.dawa.core.time

import java.time.LocalTime

object ArabicFormatters {

    private val ARABIC_DIGITS = charArrayOf(
        '٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'
    )

    /**
     * Converts any integer or number string into Eastern Arabic (Arabic-Indic) digits.
     * e.g. 12 -> ١٢
     */
    fun toArabicDigits(number: Int): String {
        return toArabicDigits(number.toString())
    }

    fun toArabicDigits(number: Long): String {
        return toArabicDigits(number.toString())
    }

    fun toArabicDigits(str: String): String {
        val sb = java.lang.StringBuilder(str.length)
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(ARABIC_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats minutes from midnight (0..1439) into 12-hour format with Eastern Arabic digits.
     * e.g. 480 (8:00 AM) -> "٨:٠٠ صباحاً"
     * e.g. 870 (14:30)   -> "٢:٣٠ مساءً"
     */
    fun formatMinutesOfDay(minutesOfDay: Int): String {
        val totalMinutes = ((minutesOfDay % 1440) + 1440) % 1440
        val hour24 = totalMinutes / 60
        val minute = totalMinutes % 60
        return formatTime(LocalTime.of(hour24, minute))
    }

    fun formatTime(time: LocalTime): String {
        val hour24 = time.hour
        val minute = time.minute
        val isAm = hour24 < 12
        var hour12 = hour24 % 12
        if (hour12 == 0) hour12 = 12

        val minuteStr = String.format("%02d", minute)
        val periodStr = if (isAm) "صباحاً" else "مساءً"

        return "${toArabicDigits(hour12)}:${toArabicDigits(minuteStr)} $periodStr"
    }

    /**
     * Quantity description for pills in Egyptian/Levantine Arabic:
     * 1 half  -> "نص قرص"
     * 2 halves (1 pill) -> "قرص واحد"
     * 4 halves (2 pills) -> "قرصين"
     * 6 halves (3 pills) -> "٣ أقراص"
     */
    fun formatPillQuantity(halves: Int): String {
        return when (halves) {
            1 -> "نص قرص"
            2 -> "قرص واحد"
            4 -> "قرصين"
            else -> {
                val pills = halves / 2
                val hasHalf = (halves % 2) != 0
                when {
                    hasHalf && pills == 0 -> "نص قرص"
                    hasHalf -> "${toArabicDigits(pills)} ونص قرص"
                    pills in 3..10 -> "${toArabicDigits(pills)} أقراص"
                    else -> "${toArabicDigits(pills)} قرص"
                }
            }
        }
    }

    /**
     * Determines broad time of day and emoji for intuitive recognition.
     */
    fun timeOfDayInfo(minutesOfDay: Int): TimeOfDayInfo {
        val hour = (minutesOfDay / 60) % 24
        return when (hour) {
            in 5..11 -> TimeOfDayInfo("الصبح", "🌅")
            in 12..14 -> TimeOfDayInfo("الضهر", "☀️")
            in 15..17 -> TimeOfDayInfo("العصر", "🌤️")
            in 18..20 -> TimeOfDayInfo("المغرب", "🌇")
            else -> TimeOfDayInfo("بليل", "🌙")
        }
    }
}

data class TimeOfDayInfo(
    val label: String,
    val emoji: String
)
