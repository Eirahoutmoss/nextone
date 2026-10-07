package com.eirahoutmoss.nextone.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Yerinde çalışan, radix-2 karmaşık FFT. [size] 2'nin kuvveti olmalıdır.
 * Ters dönüşümde 1/N ölçekleme uygulanır.
 */
class Fft(val size: Int) {
    private val cosTable: DoubleArray
    private val sinTable: DoubleArray
    private val bitRev: IntArray

    init {
        require(size >= 2 && (size and (size - 1)) == 0) { "FFT boyutu 2'nin kuvveti olmalı: $size" }
        cosTable = DoubleArray(size / 2) { cos(2.0 * PI * it / size) }
        sinTable = DoubleArray(size / 2) { sin(2.0 * PI * it / size) }
        val bits = Integer.numberOfTrailingZeros(size)
        bitRev = IntArray(size) { Integer.reverse(it) ushr (32 - bits) }
    }

    fun transform(re: DoubleArray, im: DoubleArray, inverse: Boolean) {
        for (i in 0 until size) {
            val j = bitRev[i]
            if (j > i) {
                var t = re[i]; re[i] = re[j]; re[j] = t
                t = im[i]; im[i] = im[j]; im[j] = t
            }
        }
        val sign = if (inverse) 1.0 else -1.0
        var len = 2
        while (len <= size) {
            val half = len / 2
            val step = size / len
            var start = 0
            while (start < size) {
                var k = 0
                for (j in start until start + half) {
                    val wr = cosTable[k]
                    val wi = sign * sinTable[k]
                    val l = j + half
                    val tr = re[l] * wr - im[l] * wi
                    val ti = re[l] * wi + im[l] * wr
                    re[l] = re[j] - tr
                    im[l] = im[j] - ti
                    re[j] += tr
                    im[j] += ti
                    k += step
                }
                start += len
            }
            len = len shl 1
        }
        if (inverse) {
            val s = 1.0 / size
            for (i in 0 until size) {
                re[i] *= s
                im[i] *= s
            }
        }
    }

    companion object {
        fun nextPowerOfTwo(n: Int): Int {
            var p = 1
            while (p < n) p = p shl 1
            return p
        }
    }
}
