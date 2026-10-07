package com.eirahoutmoss.nextone.tuning

import kotlin.math.ln
import kotlin.math.pow

/** Batı nota adları (Türkçe solfej) ve MIDI/frekans dönüşümleri. */
object Notes {
    val NAMES = arrayOf("Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Si")
    private val BASE = mapOf("Do" to 0, "Re" to 2, "Mi" to 4, "Fa" to 5, "Sol" to 7, "La" to 9, "Si" to 11)
    private val PATTERN = Regex("""^(Do|Re|Mi|Fa|Sol|La|Si)([#♯b♭]?)(-?\d)$""")

    /** "La4", "Si♭3", "Fa#2", "Do♯5" → MIDI numarası (La4 = 69). */
    fun parse(text: String): Int {
        val m = PATTERN.matchEntire(text.trim()) ?: throw IllegalArgumentException("Nota anlaşılamadı: '$text'")
        val (name, acc, oct) = m.destructured
        val shift = when (acc) { "#", "♯" -> 1; "b", "♭" -> -1; else -> 0 }
        return (oct.toInt() + 1) * 12 + BASE.getValue(name) + shift
    }

    fun name(midi: Int): String {
        val pc = Math.floorMod(midi, 12)
        val oct = Math.floorDiv(midi, 12) - 1
        return NAMES[pc] + oct
    }

    fun frequency(midi: Double, a4: Double = 440.0) = a4 * 2.0.pow((midi - 69.0) / 12.0)

    fun cents(f: Double, ref: Double) = 1200.0 * ln(f / ref) / ln(2.0)
}
