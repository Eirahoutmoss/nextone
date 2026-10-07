package com.eirahoutmoss.nextone.tuning

import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TuningTest {

    private fun close(a: Double, b: Double, eps: Double, msg: String = "") =
        assertTrue(abs(a - b) <= eps, "$msg beklenen $b, bulunan $a")

    // ---- Notalar ----

    @Test
    fun `nota cozumleme`() {
        assertEquals(69, Notes.parse("La4"))
        assertEquals(60, Notes.parse("Do4"))
        assertEquals(40, Notes.parse("Mi2"))
        assertEquals(70, Notes.parse("Si♭4"))
        assertEquals(66, Notes.parse("Fa#4"))
        assertEquals(23, Notes.parse("Si0"))
        assertEquals("Sol3", Notes.name(55))
        assertEquals("Do♯5", Notes.name(73))
    }

    @Test
    fun `acik tel frekanslari plandaki tabloyla ayni`() {
        close(Notes.frequency(Notes.parse("Mi2").toDouble()), 82.41, 0.005, "Gitar Mi2")
        close(Notes.frequency(Notes.parse("Sol3").toDouble()), 196.00, 0.005, "Keman Sol3")
        close(Notes.frequency(Notes.parse("Do2").toDouble()), 65.41, 0.005, "Çello Do2")
        close(Notes.frequency(Notes.parse("Si0").toDouble()), 30.87, 0.005, "Bas Si0")
    }

    // ---- 12 eşit ses ----

    @Test
    fun `bati okumasi`() {
        val r = EqualTemperament.read(440.0)
        assertEquals("La4", r.name); close(r.cents, 0.0, 1e-9); assertNull(r.koma)
        val r2 = EqualTemperament.read(446.0)
        assertEquals("La4", r2.name); close(r2.cents, 23.45, 0.01)
        val r3 = EqualTemperament.read(82.0)
        assertEquals("Mi2", r3.name); close(r3.cents, -8.57, 0.01)
    }

    @Test
    fun `referans frekansi 442`() {
        val r = EqualTemperament.read(442.0, a4 = 442.0)
        assertEquals("La4", r.name); close(r.cents, 0.0, 1e-9); close(r.targetHz, 442.0, 1e-9)
    }

    @Test
    fun `si bemol klarnet transpozisyonu`() {
        // Si♭ klarnet: yazılı Do4 → gerçek Si♭3 (−200 cent)
        val sounding = Notes.frequency(Notes.parse("Si♭3").toDouble())
        val r = EqualTemperament.read(sounding, transposeCents = -200.0)
        assertEquals("Do4", r.name); close(r.cents, 0.0, 1e-9); close(r.targetHz, sounding, 1e-9)
    }

    // ---- Makam sistemleri ----

    @Test
    fun `aeu perdeleri pisagor oranlarindan`() {
        // Dügâh = La4 = a4
        val d = MakamSystem.AEU.read(440.0)
        assertEquals("Dügâh", d.name); close(d.cents, 0.0, 1e-9); assertEquals("La4", d.western)
        // Rast = Dügâh'tan 9/8 pes
        val rastHz = 440.0 * 8.0 / 9.0
        val r = MakamSystem.AEU.read(rastHz)
        assertEquals("Rast", r.name); close(r.cents, 0.0, 1e-9); assertEquals("Sol4", r.western)
        // Segâh = Rast · 8192/6561
        val s = MakamSystem.AEU.read(rastHz * 8192.0 / 6561.0)
        assertEquals("Segâh", s.name); close(s.cents, 0.0, 1e-9)
        // Nevâ = Rast · 3/2
        assertEquals("Nevâ", MakamSystem.AEU.read(rastHz * 1.5).name)
        // Gerdaniye = Rast · 2
        assertEquals("Gerdaniye", MakamSystem.AEU.read(rastHz * 2).name)
        // Muhayyer = Dügâh · 2
        assertEquals("Muhayyer", MakamSystem.AEU.read(880.0).name)
        // Yegâh = Nevâ / 2
        assertEquals("Yegâh", MakamSystem.AEU.read(rastHz * 0.75).name)
    }

    @Test
    fun `aeu segah esit sesli si den farkli`() {
        // 12 eşit seste Si4 = La4 + 200 cent; AEU Segâh = Dügâh + 180,45 cent → Si4 bir Segâh'tan ~19,5 cent tiz
        val si4 = 440.0 * 2.0.pow(200.0 / 1200.0)
        val r = MakamSystem.AEU.read(si4)
        assertEquals("Bûselik", r.name)   // Si4 tam ses (203,91) Bûselik'e daha yakın
        close(r.cents, -3.91, 0.01)
    }

    @Test
    fun `koma sapmasi`() {
        val k = MakamSystem.KOMA_CENTS
        val dugahPlusOneKoma = 440.0 * 2.0.pow(k / 1200.0 * 0.5)  // yarım koma tiz
        val r = MakamSystem.KOMA53.read(dugahPlusOneKoma)
        assertEquals("Dügâh", r.name)
        close(r.koma!!, 0.5, 1e-9)
    }

    @Test
    fun `koma53 perde adimlari`() {
        val k = MakamSystem.KOMA_CENTS
        val rast = 440.0 * 2.0.pow(-9 * k / 1200.0)
        fun at(steps: Int) = MakamSystem.KOMA53.read(rast * 2.0.pow(steps * k / 1200.0)).name
        assertEquals("Rast", at(0))
        assertEquals("Dügâh", at(9))
        assertEquals("Segâh", at(17))
        assertEquals("Bûselik", at(18))
        assertEquals("Çârgâh", at(22))
        assertEquals("Hicaz", at(27))
        assertEquals("Nevâ", at(31))
        assertEquals("Eviç", at(48))
        assertEquals("Gerdaniye", at(53))
    }

    @Test
    fun `transpozisyonla makam okumasi`() {
        // Bağlama örneği: yazılı La (dügâh) karar, gerçek ses Do5 (+300 cent)
        val sounding = 440.0 * 2.0.pow(300.0 / 1200.0)
        val r = MakamSystem.AEU.read(sounding, transposeCents = 300.0)
        assertEquals("Dügâh", r.name); close(r.cents, 0.0, 1e-9); close(r.targetHz, sounding, 1e-9)
    }

    // ---- Tel eşleme ----

    @Test
    fun `gitar tel tanima ve histerezis`() {
        val guitar = doubleArrayOf(82.41, 110.0, 146.83, 196.0, 246.94, 329.63)
        val m = StringMatcher(guitar)
        assertEquals(0, m.match(83.0))
        assertEquals(1, m.match(109.0))
        assertEquals(5, m.match(330.0))
        // Re3 ile Sol3 arasında, Re3'e biraz daha yakın bir ses: önceki seçim (Sol3) korunur
        m.select(3)
        val between = 146.83 * 2.0.pow(240.0 / 1200.0)
        assertEquals(3, m.match(between))
    }
}
