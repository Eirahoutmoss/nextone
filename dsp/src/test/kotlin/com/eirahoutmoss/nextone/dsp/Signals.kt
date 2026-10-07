package com.eirahoutmoss.nextone.dsp

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Testler için sentetik sinyaller. */
object Signals {
    const val SR = 48_000

    fun sine(f: Double, n: Int, phase: Double = 0.3, offset: Int = 0) =
        FloatArray(n) { sin(2 * PI * f * (it + offset) / SR + phase).toFloat() }

    /** 11 harmonikli testere: tel/yaylı çalgı benzeri zengin tını. */
    fun saw(f: Double, n: Int, phase: Double = 0.3, offset: Int = 0) = FloatArray(n) { i ->
        var s = 0.0
        var h = 1
        while (h <= 11 && f * h < 20_000) {
            s += sin(2 * PI * f * h * (i + offset) / SR + phase * h) / h
            h++
        }
        s.toFloat()
    }

    /** Temeli zayıf, 2. harmoniği güçlü sinyal: oktav hatası tuzağı. */
    fun weakFundamental(f: Double, n: Int, phase: Double = 0.3) = FloatArray(n) { i ->
        val t = i.toDouble() / SR
        (0.3 * sin(2 * PI * f * t) + sin(2 * PI * 2 * f * t + phase) + 0.4 * sin(2 * PI * 3 * f * t)).toFloat()
    }

    /**
     * Karplus–Strong telli çalgı (mızrap) modeli. Ortalama filtresinin 0,5 örnek gecikmesi
     * ve kesirli gecikme için all-pass hesaba katılmıştır; üretilen perde [f] ile örtüşür.
     */
    fun pluck(f: Double, n: Int, seed: Int = 0, skip: Int = 3000, dc: Double = 0.0): FloatArray {
        val r = Random(seed)
        var p = SR / f - 0.5
        var l = p.toInt()
        var frac = p - l
        if (frac < 0.1) { l -= 1; frac += 1.0 }
        val a = (1 - frac) / (1 + frac)
        val buf = DoubleArray(l) { r.nextDouble(-1.0, 1.0) }
        val out = FloatArray(n)
        var prev = 0.0; var ax = 0.0; var ay = 0.0
        for (i in 0 until n + skip) {
            val s = buf[i % l]
            if (i >= skip) out[i - skip] = (s + dc).toFloat()
            val y = 0.997 * 0.5 * (s + prev); prev = s
            val yy = a * y + ax - a * ay; ax = y; ay = yy
            buf[i % l] = yy
        }
        return out
    }

    /** Sinyale verilen SNR'de (dB) beyaz gürültü ekler. */
    fun addNoise(x: FloatArray, snrDb: Double, seed: Int = 1): FloatArray {
        val r = java.util.Random(seed.toLong())
        var p = 0.0
        for (v in x) p += v * v
        val sigma = kotlin.math.sqrt(p / x.size / Math.pow(10.0, snrDb / 10))
        return FloatArray(x.size) { (x[it] + r.nextGaussian() * sigma).toFloat() }
    }

    fun whiteNoise(n: Int, seed: Int = 5): FloatArray {
        val r = java.util.Random(seed.toLong())
        return FloatArray(n) { (r.nextGaussian() * 0.3).toFloat() }
    }

    fun cents(f: Double, ref: Double) = 1200.0 * kotlin.math.ln(f / ref) / kotlin.math.ln(2.0)

    /** Çalgıların açık tellerini kapsayan test frekansları (Si0 → Do7). */
    val TEST_FREQUENCIES = doubleArrayOf(
        30.87, 41.20, 55.00, 65.41, 82.41, 110.00, 146.83, 196.00,
        293.66, 440.00, 659.26, 1046.50, 1568.00, 2093.00,
    )

    fun windowFor(f: Double) = if (f < 80.0) 4096 else 2048
}
