package com.eirahoutmoss.nextone.core

import java.util.Locale

/**
 * Sınıf kodu: öğretmenin bütün sınıfı ortak bir referansta buluşturması için kısa metin.
 * Biçim: `KARAR-A4` ya da `KARAR-A4-53` (53 koma sistemi). Örnekler: `RE-440`, `DO#-442`, `SOL-440-53`.
 *
 * İnternet ve bağlantı gerekmez; kod tahtaya yazılır ya da sesli söylenir.
 * Bağlama ailesinde karar sesini, bütün çalgılarda La4 frekansını (ve isteğe bağlı perde sistemini) belirler.
 */
data class ClassCode(val karar: String, val a4: Int, val koma53: Boolean = false) {

    init {
        require(karar in KARAR_NAMES) { "Bilinmeyen karar: $karar" }
        require(a4 in A4_RANGE) { "La4 ${A4_RANGE.first}–${A4_RANGE.last} Hz arasında olmalı" }
    }

    /** Tahtaya yazılacak biçim. */
    val text: String
        get() = TOKENS[KARAR_NAMES.indexOf(karar)] + "-" + a4 + if (koma53) "-53" else ""

    companion object {
        val A4_RANGE = 415..466

        /** Profillerdeki karar seçenekleriyle aynı adlar. */
        val KARAR_NAMES = listOf("Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Si")
        private val TOKENS = listOf("DO", "DO#", "RE", "RE#", "MI", "FA", "FA#", "SOL", "SOL#", "LA", "LA#", "SI")
        private val BASE = mapOf("DO" to 0, "RE" to 2, "MI" to 4, "FA" to 5, "SOL" to 7, "LA" to 9, "SI" to 11)
        private val PATTERN = Regex("""^(DO|RE|MI|FA|SOL|LA|SI)(#|B)?-?(\d{3})(?:-?(53|AEU))?$""")

        /**
         * Kullanıcının yazdığı kodu çözer; anlaşılamazsa null.
         * Türkçe klavyeye dayanıklıdır: "re-440", "Sİ-440", "sı-440", "Do♯ 442", "sib-440", "RE440".
         */
        fun parse(input: String): ClassCode? {
            val s = input.trim()
                .uppercase(Locale.ROOT)
                .replace('İ', 'I')
                .replace("♯", "#")
                .replace("♭", "B")
                .replace('–', '-')
                .replace('—', '-')
                .replace(" ", "")
            val m = PATTERN.matchEntire(s) ?: return null
            val (note, acc, hz, system) = m.destructured
            val shift = when (acc) { "#" -> 1; "B" -> -1; else -> 0 }
            val pc = Math.floorMod(BASE.getValue(note) + shift, 12)
            val a4 = hz.toInt()
            if (a4 !in A4_RANGE) return null
            return ClassCode(KARAR_NAMES[pc], a4, koma53 = system == "53")
        }
    }
}
