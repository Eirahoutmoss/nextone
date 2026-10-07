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
        assertEquals(
            listOf(
                "gitar", "gitar-12", "bas-gitar", "keman", "viyola", "viyolonsel", "kontrbas", "mandolin", "ukulele",
                "baglama-kisa-sap", "baglama-uzun-sap", "cura", "divan-sazi", "ud", "serbest",
            ),
            profiles.map { it.id },
        )
        for (p in profiles) for (t in p.tunings) {
            assertTrue(p.free || t.strings.isNotEmpty(), "${p.id}/${t.id} telsiz")
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
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings(), option = "Do")
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

    // ---- K4: yeni çalgılar ----

    @Test
    fun `her profilin her duzeni oturum acar`() {
        for (p in profiles) for (t in p.tunings) for (opt in listOf<String?>(null) + p.optionNames) {
            val s = TunerSession(p, t, TunerSettings(), opt)
            assertEquals(t.strings.size, s.strings.size)
            for (sv in s.strings) assertTrue(sv.targetHz in 25.0..2500.0, "${p.id}/${t.id}/${sv.label}: ${sv.targetHz}")
            s.update(pitch(220.0), 0)
            s.update(TrackerState.Silence, 10)
        }
    }

    @Test
    fun `bes telli bas si0`() {
        val p = profile("bas-gitar")
        val s = TunerSession(p, p.tuning("5-tel"), TunerSettings())
        close(s.strings[0].targetHz, 30.868, 0.001)
        assertEquals(4096, p.window)
        val v = s.update(pitch(s.strings[0].targetHz * 2.0.pow(-4.0 / 1200)), 0)
        assertEquals(0, v.activeString)
        close(v.signal!!.deviationCents, -4.0, 0.01)
    }

    @Test
    fun `ud arap modern`() {
        val p = profile("ud")
        val s = TunerSession(p, p.tuning("arap-modern"), TunerSettings(readingOverride = ReadingMode.WESTERN))
        assertEquals(listOf("Fa2", "La2", "Re3", "Sol3", "Do4", "Fa4"), s.strings.map { it.targetName })
    }

    @Test
    fun `cura oktavdan bagimsiz ve adlar kisa sapla ayni`() {
        val cura = profile("cura")
        val kisa = profile("baglama-kisa-sap")
        assertTrue(cura.octaveAgnostic)
        val a = TunerSession(cura, cura.tuning("bozuk"), TunerSettings())
        val b = TunerSession(kisa, kisa.tuning("bozuk"), TunerSettings())
        assertEquals(b.strings.map { it.targetName }, a.strings.map { it.targetName })
        close(a.strings[0].targetHz, 2 * b.strings[0].targetHz, 1e-6)    // cura bir oktav tiz
    }

    @Test
    fun `serbest mod si bemol klarnet`() {
        val p = profile("serbest")
        assertTrue(p.free)
        assertEquals("Transpozisyon", p.optionLabel)
        val opt = p.optionNames.first { it.startsWith("Si♭ — klarnet") }
        val s = TunerSession(p, p.tunings.first(), TunerSettings(), opt)
        assertTrue(s.strings.isEmpty())
        // Klarnette yazılı Re5 → gerçek Do5 (523,25 Hz)
        val v = s.update(pitch(523.2511 * 2.0.pow(6.0 / 1200)), 0)
        assertTrue(v.free)
        assertEquals(-1, v.activeString)
        assertEquals("Re5", v.signal!!.heardName)
        close(v.signal!!.deviationCents, 6.0, 0.01)
        close(v.signal!!.heardTargetHz, 523.2511, 0.01)
    }

    @Test
    fun `serbest mod blok flut bir oktav tiz`() {
        val p = profile("serbest")
        val s = TunerSession(p, p.tunings.first(), TunerSettings(), p.optionNames.first { it.startsWith("Blok flüt") })
        // Soprano blok flütte yazılı Do5 → gerçek Do6 (1046,5 Hz)
        assertEquals("Do5", s.update(pitch(1046.502), 0).signal!!.heardName)
    }

    @Test
    fun `serbest mod nota degisince onay sifirlanir`() {
        val p = profile("serbest")
        val s = TunerSession(p, p.tunings.first(), TunerSettings())
        var t = 0L
        var count = 0
        repeat(80) { if (s.update(pitch(440.0), t).signal!!.confirmedNow) count++; t += 20 }    // La4
        repeat(80) { if (s.update(pitch(493.883), t).signal!!.confirmedNow) count++; t += 20 }  // Si4
        assertEquals(2, count)
    }

    @Test
    fun `gecersiz secenek varsayilana duser`() {
        val p = profile("baglama-kisa-sap")
        val s = TunerSession(p, p.tuning("bozuk"), TunerSettings(), "Si♭ — tenor saksafon")
        assertEquals("La", s.optionName)
        close(s.transposeCents, 0.0, 0.0)
    }

    // ---- Tel grupları ----

    @Test
    fun `on iki telli gitar oktav teli kendi grubuna eslenir`() {
        val p = profile("gitar-12")
        val s = TunerSession(p, p.tuning("standart"), TunerSettings())
        assertEquals(listOf("Mi3"), s.strings[0].octaveNames)
        assertEquals(2, s.strings[0].count)
        // 6. grubun oktav teli Mi3 (164,81 Hz), 3 cent pes çalınmış
        val v = s.update(pitch(164.8138 * 2.0.pow(-3.0 / 1200)), 0)
        assertEquals(0, v.activeString)
        close(v.signal!!.deviationCents, -3.0, 0.01)
        // 4. grubun oktav teli Re4, 4. gruba (Re3) eşlenir; 1. grubun Mi4'üne değil
        val v2 = s.update(pitch(293.6648), 100)
        assertEquals(2, v2.activeString)
        close(v2.signal!!.deviationCents, 0.0, 0.01)
    }

    @Test
    fun `tel sayilari`() {
        fun total(id: String, tuning: String) = profile(id).tuning(tuning).strings.sumOf { it.count ?: 0 }
        assertEquals(6, total("gitar", "standart"))
        assertEquals(12, total("gitar-12", "standart"))
        assertEquals(8, total("mandolin", "standart"))
        assertEquals(7, total("baglama-kisa-sap", "bozuk"))
        assertEquals(7, total("baglama-uzun-sap", "baglama-duzeni"))
        assertEquals(11, total("ud", "turk-bolahenk"))
        assertEquals(listOf(2, 2, 3), profile("baglama-kisa-sap").tuning("bozuk").strings.map { it.count })
    }

    @Test
    fun `oktav telleri ayni nota sinifinda ve grup sayisi tutarli`() {
        for (p in profiles) for (t in p.tunings) for (st in t.strings) {
            for (m in st.octaveMidis) {
                assertEquals(Math.floorMod(st.midi, 12), Math.floorMod(m, 12), "${p.id}/${t.id}/${st.label}")
                assertTrue((st.count ?: 0) >= 1 + st.octaveMidis.size, "${p.id}/${t.id}/${st.label}: tel sayısı oktav tellerinden az")
            }
        }
    }

    @Test
    fun `gitar bilgisi naylon ve celik ayni akort`() {
        val info = profile("gitar").info ?: ""
        assertTrue("naylon" in info && "çelik" in info && "aynı akordu" in info)
    }
}
