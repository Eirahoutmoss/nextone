package com.eirahoutmoss.nextone.dsp

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

data class TrackerConfig(
    val sampleRate: Int = 48_000,
    /** Analiz penceresi. Pes çalgılarda 4096, diğerlerinde 2048. */
    val windowSize: Int = 2048,
    /** İki analiz arasındaki örnek sayısı. */
    val hopSize: Int = 512,
    val fMin: Double = 25.0,
    val fMax: Double = 2500.0,
    /** Bu seviyenin altı sessizlik sayılır (dBFS). */
    val gateDbfs: Double = -55.0,
    /** MPM netliği bunun altındaysa okuma gösterilmez. Prototipte sönmüş tel 0,19 verdi. */
    val minClarity: Double = 0.70,
    /** Netlik bunun altındaysa YIN ile ikinci görüş alınır. */
    val confirmBelowClarity: Double = 0.85,
    /** Seviye bir karede bu kadar yükselirse yeni vuruş (mızrap/yay) sayılır. */
    val onsetRiseDb: Double = 6.0,
    /** Vuruştan sonra okuma gösterilmeyen süre. */
    val onsetHoldMs: Double = 30.0,
    /** Medyan filtresi uzunluğu (kare). */
    val medianSize: Int = 5,
    /** Üstel yumuşatma katsayısı (0–1, büyük = hızlı tepki). */
    val smoothing: Double = 0.35,
    /** Ortalama değerden bu kadar uzak ve tutarlı iki okuma yeni nota demektir. */
    val newNoteCents: Double = 50.0,
)

sealed interface TrackerState {
    /** Ses seviyesi eşiğin altında. */
    data object Silence : TrackerState

    /** Ses var ama güvenilir bir perde yok (vuruş anı, gürültü, sönmüş tel). */
    data object Unclear : TrackerState

    /**
     * @param frequency yumuşatılmış frekans (arayüzde gösterilen)
     * @param raw bu karenin ham tahmini
     * @param settled medyan penceresi dolduğunda true; ibre artık güvenilir
     */
    data class Pitch(
        val frequency: Double,
        val raw: Double,
        val clarity: Double,
        val settled: Boolean,
    ) : TrackerState
}

/**
 * Kareden kareye perde takibi: gürültü kapısı → vuruş algısı → MPM (+YIN onayı)
 * → yeni nota algısı → medyan + üstel yumuşatma.
 * Yumuşatma cent alanında yapılır (frekans oranları logaritmik).
 */
class PitchTracker(val config: TrackerConfig = TrackerConfig()) {
    private val mpm = Mpm(config.windowSize, config.sampleRate)
    private val yin = Yin(config.windowSize, config.sampleRate)
    private val ring = DoubleArray(config.medianSize)
    private val sortBuf = DoubleArray(config.medianSize)
    private var ringCount = 0
    private var ringPos = 0
    private var smoothedCents = Double.NaN
    private var pendingCents = Double.NaN
    private var prevDb = -200.0
    private var holdFrames = 0
    private val onsetHoldFrames =
        ceil(config.onsetHoldMs / 1000.0 * config.sampleRate / config.hopSize).toInt()

    fun reset() {
        ringCount = 0; ringPos = 0
        smoothedCents = Double.NaN
        pendingCents = Double.NaN
        holdFrames = 0
    }

    fun process(window: FloatArray, length: Int = window.size): TrackerState {
        val n = minOf(length, window.size)
        var sum = 0.0
        var mean = 0.0
        for (i in 0 until n) mean += window[i]
        mean /= n
        for (i in 0 until n) { val v = window[i] - mean; sum += v * v }
        val rms = sqrt(sum / n)
        val db = if (rms > 0) 20.0 * log10(rms) else -200.0

        if (db < config.gateDbfs) {
            prevDb = db
            reset()
            return TrackerState.Silence
        }
        if (db - prevDb > config.onsetRiseDb) holdFrames = onsetHoldFrames
        prevDb = db
        if (holdFrames > 0) {
            holdFrames--
            return TrackerState.Unclear
        }

        val est = mpm.detect(window, n, config.fMin, config.fMax) ?: return TrackerState.Unclear
        if (est.clarity < config.minClarity) return TrackerState.Unclear
        if (est.clarity < config.confirmBelowClarity) {
            val second = yin.detect(window, n, config.fMin, config.fMax) ?: return TrackerState.Unclear
            if (abs(centsBetween(second.frequency, est.frequency)) > 50.0) return TrackerState.Unclear
        }

        val c = toCents(est.frequency)
        if (!smoothedCents.isNaN() && abs(c - smoothedCents) > config.newNoteCents) {
            // Tek bir sapkın kare ortalamayı bozmasın: iki tutarlı kare gerekirse yeni notaya geç
            if (!pendingCents.isNaN() && abs(c - pendingCents) < 30.0) {
                ringCount = 0; ringPos = 0
                smoothedCents = Double.NaN
                pendingCents = Double.NaN
            } else {
                pendingCents = c
                return TrackerState.Pitch(fromCents(smoothedCents), est.frequency, est.clarity, ringCount >= ring.size)
            }
        } else {
            pendingCents = Double.NaN
        }

        ring[ringPos] = c
        ringPos = (ringPos + 1) % ring.size
        if (ringCount < ring.size) ringCount++
        val med = median()
        smoothedCents = if (smoothedCents.isNaN()) med else smoothedCents + config.smoothing * (med - smoothedCents)

        return TrackerState.Pitch(
            frequency = fromCents(smoothedCents),
            raw = est.frequency,
            clarity = est.clarity,
            settled = ringCount >= ring.size,
        )
    }

    private fun median(): Double {
        for (i in 0 until ringCount) sortBuf[i] = ring[i]
        java.util.Arrays.sort(sortBuf, 0, ringCount)
        return if (ringCount % 2 == 1) sortBuf[ringCount / 2]
        else 0.5 * (sortBuf[ringCount / 2 - 1] + sortBuf[ringCount / 2])
    }

    companion object {
        private const val REF = 440.0
        fun toCents(f: Double) = 1200.0 * ln(f / REF) / ln(2.0)
        fun fromCents(c: Double) = REF * 2.0.pow(c / 1200.0)
        fun centsBetween(f: Double, ref: Double) = 1200.0 * ln(f / ref) / ln(2.0)
    }
}
