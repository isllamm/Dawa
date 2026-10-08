package com.family.dawa.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

class AudioRecorder(private val context: Context) {

    private val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): String {
        stopRecording()
        val file = File(audioDir, "${UUID.randomUUID()}.m4a")
        currentFile = file

        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mr
        return file.absolutePath
    }

    fun stopRecording(): String? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            currentFile?.absolutePath
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            currentFile?.delete()
            null
        }
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrEmpty()) return
        try {
            val file = File(path)
            if (file.exists() && file.startsWith(audioDir)) {
                file.delete()
            }
        } catch (_: Exception) {}
    }
}
