package com.eirahoutmoss.nextone.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClassCodeTest {

    @Test
    fun `temel kodlar`() {
        assertEquals(ClassCode("Re", 440), ClassCode.parse("RE-440"))
        assertEquals(ClassCode("Do♯", 442), ClassCode.parse("DO#-442"))
        assertEquals(ClassCode("Sol", 440, koma53 = true), ClassCode.parse("SOL-440-53"))
        assertEquals(ClassCode("Sol", 440, koma53 = false), ClassCode.parse("SOL-440-AEU"))
    }

    @Test
    fun `turkce klavye ve yazim toleransi`() {
        assertEquals(ClassCode("Re", 440), ClassCode.parse("re-440"))
        assertEquals(ClassCode("Si", 440), ClassCode.parse("Sİ-440"))     // Türkçe büyük İ
        assertEquals(ClassCode("Si", 440), ClassCode.parse("sı-440"))     // noktasız ı
        assertEquals(ClassCode("Si", 440), ClassCode.parse("si-440"))
        assertEquals(ClassCode("Do♯", 442), ClassCode.parse("Do♯ 442"))
        assertEquals(ClassCode("La♯", 440), ClassCode.parse("sib-440"))   // Si♭ = La♯
        assertEquals(ClassCode("La♯", 440), ClassCode.parse("Si♭-440"))
        assertEquals(ClassCode("Re", 440), ClassCode.parse("RE440"))
        assertEquals(ClassCode("Re", 440), ClassCode.parse("  re – 440 "))
        assertEquals(ClassCode("Si", 440), ClassCode.parse("DOB-440"))    // Do♭ = Si
    }

    @Test
    fun `gecersiz kodlar`() {
        for (bad in listOf("", "RE", "440", "XY-440", "RE-400", "RE-470", "RE-44", "RE-440-12", "RE#B-440")) {
            assertNull(ClassCode.parse(bad), "Kabul edilmemeliydi: '$bad'")
        }
    }

    @Test
    fun `butun kararlar gidip donus`() {
        for (k in ClassCode.KARAR_NAMES) for (a4 in listOf(415, 440, 442, 466)) for (k53 in listOf(false, true)) {
            val c = ClassCode(k, a4, k53)
            assertEquals(c, ClassCode.parse(c.text), c.text)
        }
    }

    @Test
    fun `karar adlari baglama profiliyle ayni`() {
        val p = Profiles.loadAll().first { it.id == "baglama-kisa-sap" }
        assertEquals(p.karar!!.options, ClassCode.KARAR_NAMES)
        assertTrue(Profiles.loadAll().filter { it.karar != null }.all { it.karar!!.options == ClassCode.KARAR_NAMES })
    }
}
