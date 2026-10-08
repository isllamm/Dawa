package com.family.dawa.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.family.dawa.R
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.DoseItem
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

class VoicePlayer(
    private val context: Context,
    private val ttsEngine: TtsEngine
) {

    private var activePlayer: MediaPlayer? = null
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Plays the Due sequence:
     * 1. Pleasant chime
     * 2. "دلوقتي معاد الدوا" + "خدي الأدوية اللي على الشاشة" (or med audio clips)
     */
    fun playDueAlert(items: List<DoseItem>, onComplete: (() -> Unit)? = null) {
        stop()
        playerScope.launch {
            playRawResource(R.raw.chime)
            delay(200)

            // Number of medications announcement
            val countText = if (items.size == 1) {
                "دلوقتي معاد الدوا. خدي الدوا اللي على الشاشة."
            } else {
                "دلوقتي معاد الدوا. دلوقتي ${ArabicFormatters.toArabicDigits(items.size)} أدوية. خدي الأدوية اللي على الشاشة."
            }

            // Speak general reminder
            ttsEngine.speak(countText)
            delay(300)

            // Speak or play custom voice for each medication
            for (item in items) {
                if (!item.audioPath.isNullOrEmpty() && File(item.audioPath).exists()) {
                    playAudioFile(item.audioPath)
                } else {
                    val medDescription = "${item.medicationName}، ${ArabicFormatters.formatPillQuantity(item.quantityHalves)}"
                    ttsEngine.speak(medDescription)
                }
                delay(400)
            }
            onComplete?.invoke()
        }
    }

    /**
     * Confirmed feedback: "تمام، خلصنا"
     */
    fun playDoneAlert() {
        stop()
        playerScope.launch {
            playRawResource(R.raw.chime)
            delay(150)
            ttsEngine.speak("تمام، تسلم إيدك يا غالية. خلصنا.")
        }
    }

    /**
     * Idle announcement: "مفيش دوا دلوقتي. الدوا الجاي الساعة..."
     */
    fun playIdleAnnouncement(nextSlotTimeFormatted: String?) {
        stop()
        playerScope.launch {
            if (nextSlotTimeFormatted != null) {
                ttsEngine.speak("مفيش دوا دلوقتي. كل حاجة تمام. الدوا الجاي $nextSlotTimeFormatted.")
            } else {
                ttsEngine.speak("مفيش دوا دلوقتي. كل حاجة تمام، ارتاحي.")
            }
        }
    }

    /**
     * Missed alert: "الدوا ده اتأخر معاده. كلّمي..."
     */
    fun playMissedAlert(contactName: String?) {
        stop()
        playerScope.launch {
            playRawResource(R.raw.chime)
            delay(200)
            val msg = if (!contactName.isNullOrEmpty()) {
                "الدوا ده اتأخر معاده. كلّمي $contactName علشان نطمن."
            } else {
                "الدوا ده اتأخر معاده. تواصلي مع العائلة أو الدكتور."
            }
            ttsEngine.speak(msg)
        }
    }

    suspend fun playAudioFile(path: String) = suspendCancellableCoroutine<Unit> { cont ->
        try {
            val file = File(path)
            if (!file.exists()) {
                cont.resume(Unit)
                return@suspendCancellableCoroutine
            }

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(path)
                setOnCompletionListener {
                    it.release()
                    if (activePlayer == it) activePlayer = null
                    if (cont.isActive) cont.resume(Unit)
                }
                setOnErrorListener { it, _, _ ->
                    it.release()
                    if (activePlayer == it) activePlayer = null
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                prepare()
                start()
            }
            activePlayer = player

            cont.invokeOnCancellation {
                player.release()
                if (activePlayer == player) activePlayer = null
            }
        } catch (e: Exception) {
            cont.resume(Unit)
        }
    }

    private suspend fun playRawResource(resId: Int) = suspendCancellableCoroutine<Unit> { cont ->
        try {
            val player = MediaPlayer.create(
                context,
                resId,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                0
            ) ?: run {
                cont.resume(Unit)
                return@suspendCancellableCoroutine
            }

            activePlayer = player
            player.setOnCompletionListener {
                it.release()
                if (activePlayer == it) activePlayer = null
                if (cont.isActive) cont.resume(Unit)
            }
            player.setOnErrorListener { it, _, _ ->
                it.release()
                if (activePlayer == it) activePlayer = null
                if (cont.isActive) cont.resume(Unit)
                true
            }
            player.start()

            cont.invokeOnCancellation {
                player.release()
                if (activePlayer == player) activePlayer = null
            }
        } catch (e: Exception) {
            cont.resume(Unit)
        }
    }

    fun stop() {
        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (_: Exception) {}
        activePlayer = null
        ttsEngine.stop()
    }
}
