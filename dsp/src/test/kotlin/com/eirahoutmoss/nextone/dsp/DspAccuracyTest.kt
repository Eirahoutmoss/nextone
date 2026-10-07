package com.eirahoutmoss.nextone.dsp

import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DspAccuracyTest {

    @Test
    fun `tek pencere MPM her sinyal turunde yarim cent icinde`() {
        for (row in AccuracySuite.singleFrame()) {
            val e = assertNotNull(row.errorCents, "Okuma yok: ${row.f} Hz, ${row.kind}")
            assertTrue(abs(e) <= 0.5, "Sapma çok büyük: ${row.f} Hz, ${row.kind}: $e cent")
        }
    }

    @Test
    fun `takipci 20 dB gurultude kararli`() {
        // 60 Hz üstü: 1 cent. 60 Hz altı (bas, kontrbas, 5 telli bas Si0): plan hedefi olan 2 cent.
        // Pes seslerde pencere başına daha az periyot düştüğü için gürültüye daha duyarlıdır.
        for (row in AccuracySuite.trackerNoisy(20.0)) {
            val e = assertNotNull(row.errorCents, "Takipçi okuma vermedi: ${row.f} Hz")
            val limit = if (row.f < 60.0) 2.0 else 1.0
            assertTrue(e <= limit, "Gürültülü sapma çok büyük: ${row.f} Hz: $e cent (sınır $limit)")
        }
    }

    @Test
    fun `sessizlik ve beyaz gurultude perde okunmaz`() {
        assertEquals(0, AccuracySuite.falsePositives())
    }

    @Test
    fun `fft ileri ve geri donusum ozdes`() {
        val n = 1024
        val fft = Fft(n)
        val re = DoubleArray(n) { kotlin.math.sin(it * 0.37) + 0.2 * kotlin.math.cos(it * 1.3) }
        val orig = re.copyOf()
        val im = DoubleArray(n)
        fft.transform(re, im, inverse = false)
        fft.transform(re, im, inverse = true)
        for (i in 0 until n) assertTrue(abs(re[i] - orig[i]) < 1e-9)
    }

    @Test
    fun `dogruluk raporu yazilir`() {
        val report = AccuracySuite.markdownReport()
        val dir = File("build/reports/nextone").apply { mkdirs() }
        File(dir, "dsp-dogruluk.md").writeText(report)
        println(report)
    }
}
