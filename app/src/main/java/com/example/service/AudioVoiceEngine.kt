package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.sin

class AudioVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        }
    }

    fun speakDialogue(
        text: String,
        languageCode: String,
        pitch: Float = 1.0f,
        speed: Float = 1.0f,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null) return

        val locale = when {
            languageCode.startsWith("hi", ignoreCase = true) -> Locale.forLanguageTag("hi-IN")
            languageCode.startsWith("mr", ignoreCase = true) -> Locale.forLanguageTag("mr-IN")
            languageCode.startsWith("ja", ignoreCase = true) -> Locale.JAPANESE
            else -> Locale.US
        }

        tts?.apply {
            language = locale
            setPitch(pitch.coerceIn(0.5f, 2.0f))
            setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    onDone?.invoke()
                }
                override fun onError(utteranceId: String?) {
                    onDone?.invoke()
                }
            })
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "anime_dialogue_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    suspend fun playCinematicCue(cueName: String) = withContext(Dispatchers.Default) {
        try {
            // Generate synthetic sound effect tone burst for anime action/magic cues
            val sampleRate = 44100
            val durationMs = when {
                cueName.contains("thunder", true) || cueName.contains("blast", true) -> 600
                cueName.contains("katana", true) || cueName.contains("slash", true) -> 250
                else -> 400
            }
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val generatedSnd = ByteArray(2 * numSamples)

            val baseFreq = when {
                cueName.contains("thunder", true) -> 120.0
                cueName.contains("katana", true) -> 880.0
                cueName.contains("neon", true) || cueName.contains("magic", true) -> 587.33
                else -> 440.0
            }

            var idx = 0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = (1.0 - (i.toDouble() / numSamples)) // Decay
                val sampleVal = sin(2 * Math.PI * baseFreq * t) * envelope
                val intVal = (sampleVal * 32767).toInt().toShort()

                generatedSnd[idx++] = (intVal.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((intVal.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
        } catch (_: Exception) {
            // Non-critical audio feedback fallback
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
