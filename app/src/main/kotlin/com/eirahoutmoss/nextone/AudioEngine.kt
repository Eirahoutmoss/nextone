package com.eirahoutmoss.nextone

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.os.SystemClock
import com.eirahoutmoss.nextone.dsp.PitchTracker
import com.eirahoutmoss.nextone.dsp.TrackerConfig
import com.eirahoutmoss.nextone.dsp.TrackerState

/**
 * Mikrofondan sürekli okuma yapan ses motoru. Ayrı bir iş parçacığında çalışır;
 * her [HOP] örnekte bir analiz yapar ve sonucu [onState] ile bildirir (ses iş parçacığından).
 */
class AudioEngine(
    private val context: Context,
    private val onState: (TrackerState, Long) -> Unit,
    private val onError: (String) -> Unit,
) {
    @Volatile private var running = false
    private var thread: Thread? = null

    var sampleRate: Int = 48_000
        private set

    val isRunning: Boolean get() = running

    @Synchronized
    fun start(windowSize: Int) {
        if (running) return
        running = true
        thread = Thread({ loop(windowSize) }, "NexTone-ses").also { it.start() }
    }

    @Synchronized
    fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }

    @SuppressLint("MissingPermission")   // İzin MainActivity'de denetlenir
    private fun loop(windowSize: Int) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        val rate = pickSampleRate()
        if (rate == null) {
            running = false
            onError("Mikrofon bu cihazda desteklenen bir biçimde açılamadı.")
            return
        }
        sampleRate = rate
        val minBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val bufBytes = maxOf(minBuf, windowSize * 4 * 2)
        val record = try {
            AudioRecord(pickSource(), rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, bufBytes)
        } catch (e: Exception) {
            running = false
            onError("Mikrofon açılamadı: ${e.message}")
            return
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            running = false
            onError("Mikrofon başka bir uygulama tarafından kullanılıyor olabilir.")
            return
        }

        val tracker = PitchTracker(TrackerConfig(sampleRate = rate, windowSize = windowSize, hopSize = HOP))
        val window = FloatArray(windowSize)
        val hop = FloatArray(HOP)
        var filled = 0
        try {
            record.startRecording()
            while (running) {
                var got = 0
                while (got < HOP && running) {
                    val n = record.read(hop, got, HOP - got, AudioRecord.READ_BLOCKING)
                    if (n < 0) throw IllegalStateException("Okuma hatası: $n")
                    got += n
                }
                if (!running) break
                System.arraycopy(window, HOP, window, 0, windowSize - HOP)
                System.arraycopy(hop, 0, window, windowSize - HOP, HOP)
                if (filled < windowSize) { filled += HOP; continue }
                onState(tracker.process(window), SystemClock.elapsedRealtime())
            }
        } catch (e: Exception) {
            onError("Ses okunurken hata: ${e.message}")
        } finally {
            try { record.stop() } catch (_: Exception) {}
            record.release()
            running = false
        }
    }

    private fun pickSampleRate(): Int? =
        intArrayOf(48_000, 44_100).firstOrNull {
            AudioRecord.getMinBufferSize(it, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT) > 0
        }

    /** İşlenmemiş kaynak (otomatik kazanç/gürültü bastırma yok) destekleniyorsa onu kullanır. */
    private fun pickSource(): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val unprocessed = am.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true"
        return if (unprocessed) MediaRecorder.AudioSource.UNPROCESSED else MediaRecorder.AudioSource.VOICE_RECOGNITION
    }

    companion object {
        const val HOP = 512
    }
}
