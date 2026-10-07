package com.eirahoutmoss.nextone.tuning

import kotlin.math.abs

/**
 * Otomatik tel tanıma: algılanan frekansı açık tel hedeflerinden en yakınına eşler.
 * Histerezis: o an seçili tel, en yakın telden en fazla [hysteresisCents] daha uzaksa
 * seçili kalır; böylece iki tel arasındaki bir seste seçim ileri geri zıplamaz.
 */
class StringMatcher(private val targetsHz: DoubleArray, private val hysteresisCents: Double = 35.0) {
    var current: Int = -1
        private set

    fun match(frequency: Double): Int {
        if (targetsHz.isEmpty()) return -1
        var best = 0
        var bestDist = Double.MAX_VALUE
        for (i in targetsHz.indices) {
            val d = abs(Notes.cents(frequency, targetsHz[i]))
            if (d < bestDist) { bestDist = d; best = i }
        }
        if (current >= 0) {
            val curDist = abs(Notes.cents(frequency, targetsHz[current]))
            if (curDist <= bestDist + hysteresisCents) return current
        }
        current = best
        return best
    }

    /** Kullanıcı tele dokunarak elle seçtiğinde. */
    fun select(index: Int) {
        require(index in targetsHz.indices)
        current = index
    }
}
