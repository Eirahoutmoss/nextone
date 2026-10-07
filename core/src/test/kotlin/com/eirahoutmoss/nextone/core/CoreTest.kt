package com.eirahoutmoss.nextone.core

import com.eirahoutmoss.nextone.dsp.TrackerState
import com.eirahoutmoss.nextone.tuning.MakamSystem
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoreTest {

    private fun close(a: Double, b: Double, eps: Double, msg: String = "") =
        assertTrue(abs(a - b) <= eps, "$msg beklenen $b, bulunan $a")

    private fun pitch(f: Double, settled: Boolean = true) = TrackerState.Pitch(f, f, 0.95, settled)

    private val profiles by lazy { Profiles.loadAll() }
    private fun profile(id: String) = profiles.first { it.id == id }

    // ---- JSON ----

    @Test
    fun `json okuyucu`() {
        val v = Json.parse("""{"a": [1, 2.5, -3e2], "b": "Dügâh \"x\"", "c": true, "d": null, "e": {}}""")
        @Suppress("UNCHECKED_CAST")
        v as Map<String, Any?>
        assertEquals(listOf(1.0, 2.5, -300.0), v["a"])
        assertEquals("Dügâh \"x\"", v["b"])
        assertEquals(true, v["c"])
        assertNull(v["d"])
    }

    @Test
    fun `json hatasi satir numarasi verir`() {
        val e = runCatching { Json.parse("{\n\"a\": [1,\n}") }.exceptionOrNull()
        assertTrue(e is IllegalArgumentException && e.message!!.contains("3. satır"), "Hata: ${e?.message}")
    }

    // ---- Profiller ----

    @Test
    fun `tum profiller yuklenir ve gecerli`() {
        assertEquals(listOf("gitar", "keman", "baglama-kisa-sap"), profiles.map { it.id })
        for (p in profiles) for (t in p.tunings) {
            assertTrue(t.strings.isNotEmpty(), "${p.id}/${t.id} telsiz")
            for (s in t.strings) assertTrue(s.midi in 20..100, "${p.id}/${t.id} ${s.note}")
        }
    }

    @Test
    fun `baglama bozuk duzen nota siniflari`() {
        val t = profile("baglama-kisa-sap").tuning("bozuk")
        assertEquals(listOf("Alt", "Orta", "Üst"), t.strings.map { it.label })
        assertEquals(listOf(9, 2, 7), t.strings.map { Math.floorMod(it.midi, 12) })  // La, Re, Sol
    }

    @Test
    fun `karar sesi transpozisyonu`() {
        val k = assertNotNull(profile("baglama-kisa-sap").karar)
        close(k.transposeCents("La"), 0.0, 0.0)
        close(k.transposeCents("Do"), 300.0, 0.0)      // La → Do: +3 yarım ton
        close(k.transposeCents("Mi"), -500.0, 0.0)     // La → Mi: −5 (pese katlanır)
        close(k.transposeCents("Fa"), -400.0, 0.0)
        close(k.transposeCents("Re"), 500.0, 0.0)
    }

    // ---- Akort oturumu: gitar ----

    @Test
    fun `gitar tel tanima ve sapma`() {
        val s = TunerSession(profile("gitar"), profile("gitar").tuning("standart"), TunerSettings())
        assertEquals(listOf("Mi2", "La2", "Re3", "Sol3", "Si3", "Mi4"), s.strings.map { it.targetName })
        val v = s.update(pitch(110.0 * 2.0.pow(-10.0 / 1200)), 0)
        assertEquals(1, v.activeString)
        val sig = assertNotNull(v.signal)
        close(sig.deviationCents, -10.0, 1e-6)
        assertTrue(!sig.inZone)
        assertEquals("La2", sig.heardName)
    }

    @Test
    fun `tamam onayi bir saniye bolgede kalinca bir kez verilir`() {
        val s = TunerSession(profile("gitar"), profile("gitar").tuning("standart"), TunerSettings())
        val f = 196.0 * 2.0.pow(1.0 / 1200)
        var confirmations = 0
        var t = 0L
        while (t <= 3000) {
            val sig = s.update(pitch(f), t).signal!!
            if (sig.confirmedNow) {
                confirmations++
                assertTrue(t >= 1000, "Onay erken geldi: $t ms")
            }
            t += 20
        }
        assertEquals(1, confirmations)
    }

    @Test
    fun `sinirda titreyen ses onayi tekrarlatmaz`() {
        val s = TunerSession(profile("gitar"), profile("gitar").tuning("standart"), TunerSettings())
        var confirmations = 0
        for (i in 0..300) {
            val c = if (i % 2 == 0) 2.8 else 3.4      // 3 cent sınırının iki yanında
            if (s.update(pitch(196.0 * 2.0.pow(c / 1200)), i * 20L).signal!!.confirmedNow) confirmations++
        }
        assertEquals(1, confirmations)
    }

    @Test
    fun `elle tel secimi`() {
        val s = TunerSession(profile("gitar"), profile("gitar").tuning("standart"), TunerSettings())
        s.selectString(0)
        val v = s.update(pitch(110.0), 0)
        assertEquals(0, v.activeString)
        assertTrue(v.manual)
        close(v.signal!!.deviationCents, 500.0, 1e-6)     // La2, Mi2'den 5 yarım ton tiz
    }

    @Test
    fun `referans frekansi 442`() {
        val s = TunerSession(profile("keman"), profile("keman").tuning("standart"), TunerSettings(a4 = 442.0))
        close(s.strings[2].targetHz, 442.0, 1e-9)
    }

    // ---- Akort oturumu: bağlama ----

    @Test
    fun `baglama makam okumasi ve perde adlari`() {
        val p = profile("baglama-kisa-sap")
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings())
        assertEquals(listOf("Dügâh", "Nevâ", "Rast"), s.strings.map { it.targetName })
        // Orta tel (yazılı Re4) makam modunda Nevâ'ya ayarlanır: Dügâh'tan saf beşli (3/2) pes
        close(s.strings[1].targetHz, 440.0 * 2.0 / 3.0, 1e-6, "Nevâ (Re4)")
        // Üst tel Rast: Dügâh'tan 9/8 pes, bir oktav aşağıda
        close(s.strings[2].targetHz, 440.0 * 8.0 / 9.0 / 2.0, 1e-6, "Kaba Rast (Sol3)")
    }

    @Test
    fun `baglama oktavdan bagimsiz eslesme`() {
        val p = profile("baglama-kisa-sap")
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings())
        // Alt tel La, ama bir oktav pes çalınmış ve 5 cent tiz
        val v = s.update(pitch(220.0 * 2.0.pow(5.0 / 1200)), 0)
        assertEquals(0, v.activeString)
        close(v.signal!!.deviationCents, 5.0, 1e-6)
        assertEquals("Dügâh", v.signal!!.heardName)
    }

    @Test
    fun `baglama karar do`() {
        val p = profile("baglama-kisa-sap")
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings(), karar = "Do")
        // Yazılı La (Dügâh) gerçek Do'ya ayarlanır: 440 · 2^(300/1200)
        close(s.strings[0].targetHz, 440.0 * 2.0.pow(300.0 / 1200), 1e-6)
        assertEquals("Dügâh", s.strings[0].targetName)
        val v = s.update(pitch(s.strings[0].targetHz), 0)
        assertEquals(0, v.activeString)
        close(v.signal!!.deviationCents, 0.0, 1e-6)
        assertEquals("Dügâh", v.signal!!.heardName)
        assertEquals("La", v.signal!!.heardAltName)   // yazılı ad, oktavsız
    }

    @Test
    fun `koma53 secilince bati disi sistem kullanilir`() {
        val p = profile("baglama-kisa-sap")
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings(makamSystemId = "koma53"))
        assertEquals(MakamSystem.KOMA53, s.system)
        val k = MakamSystem.KOMA_CENTS
        // Nevâ = Dügâh + 22 koma → Re4 = Dügâh − (53−22)… oktavdan bağımsız: hedefin nota sınıfı Nevâ
        val v = s.update(pitch(s.strings[1].targetHz * 2.0.pow(k / 1200)), 0)
        close(v.signal!!.heardKoma!!, 1.0, 1e-6)
    }
}
