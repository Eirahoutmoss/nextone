package com.eirahoutmoss.nextone.dsp

import com.eirahoutmoss.nextone.dsp.Signals.SR
import com.eirahoutmoss.nextone.dsp.Signals.cents
import kotlin.math.abs

/**
 * Doğruluk ölçümleri. JUnit testleri bu fonksiyonları çağırır ve eşikleri denetler;
 * aynı fonksiyonlar Markdown raporu da üretir.
 */
object AccuracySuite {

    data class Row(val f: Double, val kind: String, val errorCents: Double?)

    /** Tek pencere MPM: sinüs, testere, zayıf temel, mızrap (DC kaymalı). */
    fun singleFrame(): List<Row> {
        val rows = mutableListOf<Row>()
        for (f in Signals.TEST_FREQUENCIES) {
            val n = Signals.windowFor(f)
            val mpm = Mpm(n, SR)
            fun err(x: FloatArray) = mpm.detect(x)?.let { cents(it.frequency, f) }
            rows += Row(f, "sinüs", err(Signals.sine(f, n)))
            rows += Row(f, "testere", err(Signals.saw(f, n)))
            rows += Row(f, "zayıf temel", err(Signals.weakFundamental(f, n)))
            rows += Row(f, "mızrap+DC", err(Signals.pluck(f, n, seed = 1, skip = 2000, dc = 0.3)))
        }
        return rows
    }

    /** Takipçi ile gürültülü sinyal: 20 dB SNR, 40 kare; yerleştikten sonraki en büyük sapma. */
    fun trackerNoisy(snrDb: Double = 20.0): List<Row> {
        val rows = mutableListOf<Row>()
        for (f in Signals.TEST_FREQUENCIES) {
            val n = Signals.windowFor(f)
            val cfg = TrackerConfig(windowSize = n, hopSize = 512)
            val tr = PitchTracker(cfg)
            val frames = 40
            val long = Signals.addNoise(Signals.saw(f, n + cfg.hopSize * frames), snrDb, seed = 11)
            var worst = 0.0
            var any = false
            for (k in 0 until frames) {
                val w = long.copyOfRange(k * cfg.hopSize, k * cfg.hopSize + n)
                val s = tr.process(w)
                if (s is TrackerState.Pitch && s.settled) {
                    any = true
                    worst = maxOf(worst, abs(cents(s.frequency, f)))
                }
            }
            rows += Row(f, "testere ${snrDb.toInt()} dB, takipçi", if (any) worst else null)
        }
        return rows
    }

    /** Okuma OLMAMASI gereken durumlar: sessizlik ve beyaz gürültü. Dönen değer: yanlış okuma sayısı. */
    fun falsePositives(): Int {
        var bad = 0
        val tr = PitchTracker(TrackerConfig())
        repeat(20) {
            if (tr.process(FloatArray(2048)) is TrackerState.Pitch) bad++
        }
        val tr2 = PitchTracker(TrackerConfig())
        val noise = Signals.whiteNoise(2048 + 512 * 40)
        for (k in 0 until 40) {
            val s = tr2.process(noise.copyOfRange(k * 512, k * 512 + 2048))
            if (s is TrackerState.Pitch) bad++
        }
        return bad
    }

    fun markdownReport(): String {
        val sb = StringBuilder()
        sb.appendLine("# NexTone — DSP doğruluk raporu")
        sb.appendLine()
        sb.appendLine("Örnekleme: $SR Hz. Pencere: <80 Hz için 4096, diğerleri 2048 örnek. Değerler cent cinsinden sapmadır (+ tiz, − pes).")
        sb.appendLine()
        sb.appendLine("## Tek pencere (MPM)")
        sb.appendLine()
        val single = singleFrame()
        val kinds = single.map { it.kind }.distinct()
        sb.appendLine("| Frekans (Hz) | " + kinds.joinToString(" | ") + " |")
        sb.appendLine("|---:|" + kinds.joinToString("") { "---:|" })
        for (f in Signals.TEST_FREQUENCIES) {
            val cells = kinds.map { k ->
                single.first { it.f == f && it.kind == k }.errorCents?.let { "%+.3f".format(it) } ?: "okuma yok"
            }
            sb.appendLine("| %.2f | ".format(f) + cells.joinToString(" | ") + " |")
        }
        sb.appendLine()
        sb.appendLine("## Takipçi, gürültülü sinyal (20 dB SNR, yerleştikten sonra en büyük sapma)")
        sb.appendLine()
        sb.appendLine("| Frekans (Hz) | En büyük sapma |")
        sb.appendLine("|---:|---:|")
        for (r in trackerNoisy()) {
            sb.appendLine("| %.2f | %s |".format(r.f, r.errorCents?.let { "%.3f".format(it) } ?: "okuma yok"))
        }
        sb.appendLine()
        sb.appendLine("## Yanlış okuma testi")
        sb.appendLine()
        sb.appendLine("Sessizlik (20 kare) + beyaz gürültü (40 kare) içinde verilen perde okuması: **${falsePositives()}**")
        return sb.toString()
    }
}
