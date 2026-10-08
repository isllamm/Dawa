package com.family.dawa.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isArabicAvailable = false

    fun init() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val arabicEgypt = Locale("ar", "EG")
                val resultEgypt = engine.isLanguageAvailable(arabicEgypt)
                if (resultEgypt >= TextToSpeech.LANG_AVAILABLE) {
                    engine.language = arabicEgypt
                    isArabicAvailable = true
                } else {
                    val genericArabic = Locale("ar")
                    val resultGeneric = engine.isLanguageAvailable(genericArabic)
                    if (resultGeneric >= TextToSpeech.LANG_AVAILABLE) {
                        engine.language = genericArabic
                        isArabicAvailable = true
                    }
                }
                engine.setPitch(1.0f)
                engine.setSpeechRate(0.85f) // slightly slower for elderly comprehension
            }
            isInitialized = true
        }
    }

    fun isReadyForArabic(): Boolean = isInitialized && isArabicAvailable

    suspend fun speak(text: String) = suspendCancellableCoroutine<Unit> { continuation ->
        val engine = tts
        if (!isReadyForArabic() || engine == null) {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val utteranceId = "dawa_tts_${System.currentTimeMillis()}"
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (continuation.isActive) continuation.resume(Unit)
            }
            override fun onError(utteranceId: String?) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        continuation.invokeOnCancellation {
            engine.stop()
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
