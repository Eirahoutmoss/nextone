package com.eirahoutmoss.nextone.dsp

/**
 * YIN perde tespiti (de Cheveigné & Kawahara, 2002). NexTone'da ikincil yöntemdir:
 * MPM'in netliği sınırdayken ikinci görüş olarak kullanılır (bkz. [PitchTracker]).
 * Fark fonksiyonu FFT ile çapraz korelasyondan hesaplanır.
 */
class Yin(val maxWindow: Int, val sampleRate: Int = 48_000) {
    private val fftSize = Fft.nextPowerOfTwo(2 * maxWindow)
    private val fft = Fft(fftSize)
    private val xr = DoubleArray(fftSize)
    private val xi = DoubleArray(fftSize)
    private val ar = DoubleArray(fftSize)
    private val ai = DoubleArray(fftSize)
    private val x = DoubleArray(maxWindow)
    private val cum = DoubleArray(maxWindow + 1)
    private val d = DoubleArray(maxWindow / 2)
    private val cmnd = DoubleArray(maxWindow / 2)

    fun detect(
        frame: FloatArray,
        length: Int = frame.size,
        fMin: Double = 25.0,
        fMax: Double = 2500.0,
        threshold: Double = 0.12,
    ): PitchEstimate? {
        val n = minOf(length, maxWindow, frame.size)
        val w = n / 2
        if (w < 32) return null
        var mean = 0.0
        for (i in 0 until n) mean += frame[i]
        mean /= n
        for (i in 0 until n) x[i] = frame[i] - mean

        // r_W(τ) = Σ_{j<W} x_j x_{j+τ}  →  IFFT( X · conj(A) ), A = x[0:W]
        for (i in 0 until fftSize) {
            xr[i] = if (i < n) x[i] else 0.0; xi[i] = 0.0
            ar[i] = if (i < w) x[i] else 0.0; ai[i] = 0.0
        }
        fft.transform(xr, xi, inverse = false)
        fft.transform(ar, ai, inverse = false)
        for (i in 0 until fftSize) {
            val r = xr[i] * ar[i] + xi[i] * ai[i]
            val im = xi[i] * ar[i] - xr[i] * ai[i]
            xr[i] = r; xi[i] = im
        }
        fft.transform(xr, xi, inverse = true)

        cum[0] = 0.0
        for (i in 0 until n) cum[i + 1] = cum[i] + x[i] * x[i]
        val e0 = cum[w]
        if (e0 <= 1e-12) return null
        for (tau in 0 until w) {
            val et = cum[tau + w] - cum[tau]
            d[tau] = e0 + et - 2.0 * xr[tau]
        }
        d[0] = 0.0
        cmnd[0] = 1.0
        var running = 0.0
        for (tau in 1 until w) {
            running += d[tau]
            cmnd[tau] = if (running > 0.0) d[tau] * tau / running else 1.0
        }

        val tauMin = maxOf(2, (sampleRate / fMax).toInt())
        val tauMax = minOf((sampleRate / fMin).toInt(), w - 2)
        var tau = tauMin
        var found = -1
        while (tau < tauMax) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 < tauMax && cmnd[tau + 1] < cmnd[tau]) tau++
                found = tau
                break
            }
            tau++
        }
        if (found < 0) {
            var best = tauMin
            for (t in tauMin until tauMax) if (cmnd[t] < cmnd[best]) best = t
            found = best
        }
        val a = cmnd[found - 1]; val b = cmnd[found]; val c = cmnd[found + 1]
        val den = a - 2.0 * b + c
        val shift = if (den != 0.0) 0.5 * (a - c) / den else 0.0
        val period = found + shift
        if (period <= 0.0) return null
        return PitchEstimate(sampleRate / period, (1.0 - b).coerceIn(0.0, 1.0))
    }
}
