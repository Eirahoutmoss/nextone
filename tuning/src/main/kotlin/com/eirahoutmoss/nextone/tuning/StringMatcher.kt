package com.eirahoutmoss.nextone.tuning

import kotlin.math.abs

/**
 * Otomatik tel tanıma: algılanan frekansı açık tel hedeflerinden en yakınına eşler.
 * Histerezis: o an seçili tel, en yakın telden en fazla [hysteresisCents] daha uzaksa
 * seçili kalır; böylece iki tel arasındaki bir seste seçim ileri geri zıplamaz.
 *
 * [octaveAgnostic]: bağlama ailesi gibi tel gruplarında oktav teli bulunan çalgılarda
 * uzaklık oktavdan bağımsız ölçülür (yalnızca nota sınıfı eşlenir).
 */
class StringMatcher(
    private val targetsHz: DoubleArray,
    private val hysteresisCents: Double = 35.0,
    private val octaveAgnostic: Boolean = false,
    /**
     * Grup başına ek hedefler: aynı tel grubundaki oktav telleri (ör. 12 telli gitarın
     * 6. grubunda Mi2'nin yanındaki Mi3). Sapma, gruptaki en yakın hedefe göre ölçülür.
     */
    private val extraTargetsHz: List<DoubleArray> = emptyList(),
) {
    var current: Int = -1
        private set

    /** Hedefe göre sapma (cent). Oktavdan bağımsız modda [-600, 600) aralığına katlanır. */
    fun deviation(frequency: Double, index: Int): Double {
        val c = Notes.cents(frequency, targetsHz[index])
        if (octaveAgnostic) return fold(c)
        var best = c
        extraTargetsHz.getOrNull(index)?.forEach { t ->
            val e = Notes.cents(frequency, t)
            if (abs(e) < abs(best)) best = e
        }
        return best
    }

    fun match(frequency: Double): Int {
        if (targetsHz.isEmpty()) return -1
        var best = 0
        var bestDist = Double.MAX_VALUE
        for (i in targetsHz.indices) {
            val d = abs(deviation(frequency, i))
            if (d < bestDist) { bestDist = d; best = i }
        }
        if (current >= 0) {
            val curDist = abs(deviation(frequency, current))
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

    companion object {
        fun fold(cents: Double): Double {
            var c = cents % 1200.0
            if (c >= 600.0) c -= 1200.0
            if (c < -600.0) c += 1200.0
            return c
        }
    }
}
