package com.family.dawa

import com.family.dawa.core.time.ArabicFormatters
import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicFormattersTest {

    @Test
    fun testToArabicDigits() {
        assertEquals("١٢٣٤٥٦٧٨٩٠", ArabicFormatters.toArabicDigits("1234567890"))
        assertEquals("٠٨:٣٠", ArabicFormatters.toArabicDigits("08:30"))
    }

    @Test
    fun testFormatPillQuantity() {
        assertEquals("نص قرص", ArabicFormatters.formatPillQuantity(1))
        assertEquals("قرص واحد", ArabicFormatters.formatPillQuantity(2))
        assertEquals("قرصين", ArabicFormatters.formatPillQuantity(4))
        assertEquals("٣ أقراص", ArabicFormatters.formatPillQuantity(6))
        assertEquals("٤ أقراص", ArabicFormatters.formatPillQuantity(8))
    }

    @Test
    fun testFormatMinutesOfDay() {
        // 480 = 8:00 AM
        assertEquals("٨:٠٠ صباحاً", ArabicFormatters.formatMinutesOfDay(480))
        // 840 = 2:00 PM (14:00)
        assertEquals("٢:٠٠ مساءً", ArabicFormatters.formatMinutesOfDay(840))
        // 1200 = 8:00 PM (20:00)
        assertEquals("٨:٠٠ مساءً", ArabicFormatters.formatMinutesOfDay(1200))
    }
}
