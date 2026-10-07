package com.eirahoutmoss.nextone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Kulakla akort için referans sesi. Saf sinüs pes seslerde telefon hoparlöründen
 * duyulmadığı için 2. ve 3. harmonik eklenir; tıklama olmaması için yumuşak giriş/çıkış.
 */
object ReferenceTone {
    private const val RATE = 48_000
    private var track: AudioTrack? = null

    @Synchronized
    fun play(frequency: Double, seconds: Double = 2.0) {
        stop()
        val n = (RATE * seconds).toInt()
        val attack = (RATE * 0.02).toInt()
        val release = (RATE * 0.35).toInt()
        val pcm = ShortArray(n)
        // Çok pes seslerde temel neredeyse duyulmaz; harmonikler perdeyi taşır
        val h2 = if (frequency < 150) 0.8 else 0.5
        val h3 = if (frequency < 150) 0.5 else 0.25
        val norm = 1.0 / (1.0 + h2 + h3)
        for (i in 0 until n) {
            val t = i.toDouble() / RATE
            val env = min(1.0, min(i.toDouble() / attack, (n - i).toDouble() / release))
            val s = sin(2 * PI * frequency * t) +
                h2 * sin(2 * PI * 2 * frequency * t) +
                h3 * sin(2 * PI * 3 * frequency * t)
            pcm[i] = (s * norm * env * 0.7 * Short.MAX_VALUE).toInt().toShort()
        }
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(n * 2)
            .build()
        t.write(pcm, 0, n)
        t.play()
        track = t
    }

    @Synchronized
    fun stop() {
        track?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
        }
        track = null
    }
}
