package com.family.dawa.data.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.family.dawa.data.db.DawaDatabase
import com.family.dawa.data.repo.ContactRepository
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DemoSeeder(
    private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val database: DawaDatabase
) {

    suspend fun seedIfNeeded() {
        val settings = settingsRepository.getSettings()
        if (settings.demoSeeded) return

        forceSeed()
    }

    /**
     * Replaces everything with the demo data. Only reachable in debug builds.
     * It deletes the existing medicines, history, contact and their photo/audio files first, so
     * demo medicines are never mixed with real ones. Settings and the admin PIN are kept.
     */
    suspend fun forceSeed() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        listOf("photos", "audio").forEach { dirName ->
            File(context.filesDir, dirName).listFiles()?.forEach { it.delete() }
        }
        seedDemoData()
    }

    private suspend fun seedDemoData() {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

        // Create 4 distinct pill image files so Grandma sees actual pill/box photos
        val fileA = generatePillBitmapFile(photosDir, "demo_med_a.png", "A", 0xFF2B78E4.toInt(), "قرص أزرق مدور")
        val fileB = generatePillBitmapFile(photosDir, "demo_med_b.png", "B", 0xFFE67E22.toInt(), "كبسولة برتقالي")
        val fileC = generatePillBitmapFile(photosDir, "demo_med_c.png", "C", 0xFF27AE60.toInt(), "فيتامين أخضر")
        val fileD = generatePillBitmapFile(photosDir, "demo_med_d.png", "D", 0xFF8E44AD.toInt(), "قرص بنفسجي بيضاوي")
        val fileMama = generateContactBitmapFile(photosDir, "demo_contact_mama.png", "ماما")

        // 1. Medicine A: 08:00 AM, 1 pill (2 halves)
        val medA = Medication(
            name = "بانادول أزرق للصداع",
            strength = "٥٠٠ مجم",
            notes = "قرص واحد مع كوب ماء كبير",
            colorTag = 0xFF2B78E4.toInt(),
            shapeTag = "CIRCLE"
        )
        medicationRepository.saveMedication(
            medication = medA,
            photos = listOf(MedicationPhoto(path = fileA.absolutePath, isPrimary = true)),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    timeOfDayMinutes = 480, // 08:00 AM
                    quantityHalves = 2,     // 1 pill
                    mealRelation = MealRelation.BEFORE_BREAKFAST
                )
            )
        )

        // 2. Medicine B: 08:00 AM, 2 pills (4 halves)
        val medB = Medication(
            name = "كونكور للضغط",
            strength = "٥ مجم",
            notes = "قرصين بعد الإفطار مباشرة",
            colorTag = 0xFFE67E22.toInt(),
            shapeTag = "OVAL"
        )
        medicationRepository.saveMedication(
            medication = medB,
            photos = listOf(MedicationPhoto(path = fileB.absolutePath, isPrimary = true)),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    timeOfDayMinutes = 480, // 08:00 AM
                    quantityHalves = 4,     // 2 pills
                    mealRelation = MealRelation.AFTER_BREAKFAST
                )
            )
        )

        // 3. Medicine C: 02:00 PM, 1 pill (2 halves)
        val medC = Medication(
            name = "مكمل فيتامين د",
            strength = "١٠٠٠ وحدة",
            notes = "قرص واحد بعد الغداء",
            colorTag = 0xFF27AE60.toInt(),
            shapeTag = "CAPSULE"
        )
        medicationRepository.saveMedication(
            medication = medC,
            photos = listOf(MedicationPhoto(path = fileC.absolutePath, isPrimary = true)),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    timeOfDayMinutes = 840, // 02:00 PM (14:00)
                    quantityHalves = 2,     // 1 pill
                    mealRelation = MealRelation.AFTER_LUNCH
                )
            )
        )

        // 4. Medicine D: 08:00 PM, 1 pill (2 halves)
        val medD = Medication(
            name = "أوميجا ٣ مسائي",
            strength = "١٠٠٠ مجم",
            notes = "كبسولة واحدة بعد العشاء",
            colorTag = 0xFF8E44AD.toInt(),
            shapeTag = "OVAL"
        )
        medicationRepository.saveMedication(
            medication = medD,
            photos = listOf(MedicationPhoto(path = fileD.absolutePath, isPrimary = true)),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    timeOfDayMinutes = 1200, // 08:00 PM (20:00)
                    quantityHalves = 2,      // 1 pill
                    mealRelation = MealRelation.AFTER_DINNER
                )
            )
        )

        // Default Contact: Mama
        contactRepository.savePrimaryContact(
            Contact(
                name = "ماما",
                phone = "01012345678",
                photoPath = fileMama.absolutePath
            )
        )

        settingsRepository.setDemoSeeded(true)
    }

    private fun generatePillBitmapFile(dir: File, fileName: String, letter: String, color: Int, label: String): File {
        val file = File(dir, fileName)
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background card
        val bgPaint = Paint().apply {
            this.color = 0xFFF0EFEB.toInt()
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(16f, 16f, size - 16f, size - 16f), 48f, 48f, bgPaint)

        // Pill shape in center
        val pillPaint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(110f, 150f, size - 110f, size - 150f), 90f, 90f, pillPaint)

        // Letter inside pill
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            textSize = 100f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(letter, size / 2f, size / 2f + 35f, textPaint)

        // Label below
        val labelPaint = Paint().apply {
            this.color = 0xFF333333.toInt()
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(label, size / 2f, size - 60f, labelPaint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    private fun generateContactBitmapFile(dir: File, fileName: String, name: String): File {
        val file = File(dir, fileName)
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Circle avatar in warm rose
        val avatarPaint = Paint().apply {
            this.color = 0xFFE91E63.toInt()
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, 220f, avatarPaint)

        val textPaint = Paint().apply {
            this.color = Color.WHITE
            textSize = 120f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("👤", size / 2f, size / 2f - 20f, textPaint)
        textPaint.textSize = 64f
        canvas.drawText(name, size / 2f, size / 2f + 110f, textPaint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }
}
