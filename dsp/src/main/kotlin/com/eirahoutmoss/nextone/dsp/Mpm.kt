package com.eirahoutmoss.nextone.dsp

/** Tek bir analiz penceresinin sonucu. [clarity]: 0–1 arası, periyodikliğin netliği. */
data class PitchEstimate(val frequency: Double, val clarity: Double)

/**
 * McLeod Pitch Method (MPM) — normalize kare fark fonksiyonu (NSDF) ile perde tespiti.
 *
 * Prototipte doğrulanan iyileştirmeler:
 *  - Analiz öncesi DC temizliği (aksi halde NSDF negatife inmez ve tepe bulunamaz).
 *  - Çok periyotlu inceltme: bulunan T periyodu, pencerenin yarısına sığan en uzak
 *    M. periyot tepesinden T = τ_M / M ile yeniden hesaplanır. Tiz seslerde
 *    parabolik interpolasyon hatasını M kat küçültür.
 *
 * Nesne tamponlarını önceden ayırır; ses döngüsünde bellek ayırmaz. İş parçacığı güvenli değildir.
 */
class Mpm(val maxWindow: Int, val sampleRate: Int = 48_000) {
    private val fftSize = Fft.nextPowerOfTwo(2 * maxWindow)
    private val fft = Fft(fftSize)
    private val re = DoubleArray(fftSize)
    private val im = DoubleArray(fftSize)
    private val x = DoubleArray(maxWindow)
    private val nsdf = DoubleArray(maxWindow)
    private val cum = DoubleArray(maxWindow + 1)
    private val peaks = IntArray(maxWindow / 2 + 1)

    /**
     * @param frame örnekler; ilk [length] örnek kullanılır.
     * @param k tepe seçme eşiği (en yüksek tepenin oranı).
     * @return tahmin; periyodik yapı bulunamazsa null.
     */
    fun detect(
        frame: FloatArray,
        length: Int = frame.size,
        fMin: Double = 25.0,
        fMax: Double = 2500.0,
        k: Double = 0.93,
    ): PitchEstimate? {
        val n = minOf(length, maxWindow, frame.size)
        if (n < 64) return null

        // 1) DC temizliği
        var mean = 0.0
        for (i in 0 until n) mean += frame[i]
        mean /= n
        for (i in 0 until n) x[i] = frame[i] - mean

        // 2) Otokorelasyon (FFT ile)
        for (i in 0 until fftSize) {
            re[i] = if (i < n) x[i] else 0.0
            im[i] = 0.0
        }
        fft.transform(re, im, inverse = false)
        for (i in 0 until fftSize) {
            re[i] = re[i] * re[i] + im[i] * im[i]
            im[i] = 0.0
        }
        fft.transform(re, im, inverse = true)

        // 3) m'(τ) = Σ x_j² + Σ x_{j+τ}²  (kümülatif toplamlarla)
        cum[0] = 0.0
        for (i in 0 until n) cum[i + 1] = cum[i] + x[i] * x[i]
        if (cum[n] <= 1e-12) return null
        for (tau in 0 until n) {
            val m = cum[n - tau] + (cum[n] - cum[tau])
            nsdf[tau] = if (m > 0.0) 2.0 * re[tau] / m else 0.0
        }

        // 4) Tepe seçimi
        val tauMin = maxOf(2, (sampleRate / fMax).toInt())
        val tauMax = minOf((sampleRate / fMin).toInt() + 2, n - 2)
        var count = 0
        var i = 1
        while (i < tauMax && nsdf[i] > 0.0) i++          // ilk pozitif bölgeyi (τ≈0) atla
        while (i < tauMax) {
            while (i < tauMax && nsdf[i] <= 0.0) i++
            var best = -1
            while (i < tauMax && nsdf[i] > 0.0) {
                if (best < 0 || nsdf[i] > nsdf[best]) best = i
                i++
            }
            if (best >= tauMin && count < peaks.size) peaks[count++] = best
        }
        if (count == 0) return null
        var highest = 0.0
        for (p in 0 until count) if (nsdf[peaks[p]] > highest) highest = nsdf[peaks[p]]
        val threshold = k * highest
        var chosen = -1
        for (p in 0 until count) {
            if (nsdf[peaks[p]] >= threshold) { chosen = peaks[p]; break }
        }
        if (chosen < 0) return null

        val (t0, clarity) = parabolic(nsdf, chosen)
        if (t0 <= 0.0) return null

        // 5) Çok periyotlu inceltme
        val periods = ((n / 2) / t0).toInt()
        var period = t0
        if (periods >= 2) {
            val centre = Math.round(periods * t0).toInt()
            val lo = maxOf(1, centre - 3)
            val hi = minOf(n - 2, centre + 3)
            var best = lo
            for (j in lo..hi) if (nsdf[j] > nsdf[best]) best = j
            if (nsdf[best] > 0.5 * clarity) {
                val (tm, _) = parabolic(nsdf, best)
                val refined = tm / periods
                // İnceltme ancak aynı periyodu doğruluyorsa kabul edilir (±%2)
                if (kotlin.math.abs(refined - t0) / t0 < 0.02) period = refined
            }
        }
        return PitchEstimate(sampleRate / period, clarity.coerceIn(0.0, 1.0))
    }

    private fun parabolic(y: DoubleArray, i: Int): Pair<Double, Double> {
        if (i <= 0 || i >= y.size - 1) return i.toDouble() to y[i]
        val a = y[i - 1]; val b = y[i]; val c = y[i + 1]
        val d = a - 2.0 * b + c
        if (d == 0.0) return i.toDouble() to b
        val p = 0.5 * (a - c) / d
        return (i + p) to (b - 0.25 * (a - c) * p)
    }
}
