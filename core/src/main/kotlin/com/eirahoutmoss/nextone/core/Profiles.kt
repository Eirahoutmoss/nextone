package com.eirahoutmoss.nextone.core

import com.eirahoutmoss.nextone.tuning.Notes

enum class ReadingMode { WESTERN, MAKAM }

/**
 * Bir tel grubu (course). [count]: gruptaki tel sayısı (bilinmiyorsa null).
 * [octaveNotes]: gruptaki oktav tellerinin notaları (ör. 12 telli gitarda "Mi3"); ana nota [note].
 */
data class StringSpec(
    val label: String,
    val note: String,
    val count: Int? = null,
    val octaveNotes: List<String> = emptyList(),
) {
    val midi: Int = Notes.parse(note)
    val octaveMidis: List<Int> = octaveNotes.map { Notes.parse(it) }
}

data class TuningSpec(
    val id: String,
    val name: String,
    val strings: List<StringSpec>,
    val source: String?,
    val verified: Boolean,
    val note: String?,
)

/**
 * Karar sesi (bağlama ailesi): yazılı karar perdesinin ([written]) gerçekte hangi sesle
 * çalındığı. Transpozisyon yarım ton farkıdır; −6…+5 aralığına katlanır, böylece
 * referans sesi yazılı oktava en yakın yerde çalar.
 */
data class KararSpec(val written: String, val default: String, val options: List<String>) {
    fun transposeCents(karar: String): Double {
        val diff = Math.floorMod(pitchClass(karar) - pitchClass(written), 12)
        val folded = if (diff > 5) diff - 12 else diff
        return folded * 100.0
    }

    companion object {
        fun pitchClass(name: String): Int = Math.floorMod(Notes.parse(name + "4"), 12)
    }
}

/** Transpoze eden çalgı seçeneği: gerçek ses = yazılı nota + [cents]. */
data class TranspositionOption(val name: String, val cents: Double)

data class InstrumentProfile(
    val id: String,
    val name: String,
    val family: String,
    val defaultReading: ReadingMode,
    val window: Int,
    val octaveAgnostic: Boolean,
    val karar: KararSpec?,
    val tunings: List<TuningSpec>,
    /** Çalgıyla ilgili kısa bilgi (ör. tel düzeni); arayüzde gösterilir. */
    val info: String? = null,
    /** Serbest (kromatik) mod: tel yok, her ses en yakın perdeye göre okunur. */
    val free: Boolean = false,
    val transpositions: List<TranspositionOption>? = null,
) {
    fun tuning(id: String?): TuningSpec = tunings.firstOrNull { it.id == id } ?: tunings.first()

    /** Seçici başlığı: bağlama ailesinde "Karar", üflemelilerde "Transpozisyon"; yoksa null. */
    val optionLabel: String?
        get() = when {
            karar != null -> "Karar"
            transpositions != null -> "Transpozisyon"
            else -> null
        }

    val optionNames: List<String>
        get() = karar?.options ?: transpositions?.map { it.name } ?: emptyList()

    val defaultOption: String?
        get() = karar?.default ?: transpositions?.firstOrNull()?.name

    fun transposeCents(option: String?): Double = when {
        karar != null -> karar.transposeCents(option?.takeIf { it in karar.options } ?: karar.default)
        transpositions != null -> (transpositions.firstOrNull { it.name == option } ?: transpositions.first()).cents
        else -> 0.0
    }
}

object Profiles {
    /** Kaynaklardan (jar/APK içindeki `profiles/`) tüm profilleri yükler. */
    fun loadAll(): List<InstrumentProfile> {
        val ids = (Json.parse(resource("index.json")) as List<*>).map { it as String }
        return ids.map { parse(resource("$it.json")) }
    }

    private fun resource(name: String): String {
        val stream = Profiles::class.java.getResourceAsStream("/profiles/$name")
            ?: throw IllegalStateException("Profil bulunamadı: $name")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun parse(text: String): InstrumentProfile {
        @Suppress("UNCHECKED_CAST")
        val m = Json.parse(text) as Map<String, Any?>
        fun str(map: Map<String, Any?>, k: String): String =
            map[k] as? String ?: throw IllegalArgumentException("'$k' alanı eksik (${m["id"]})")

        val free = m["serbest"] as? Boolean ?: false
        val rawTunings = (m["duzenler"] as? List<*>)
            ?: if (free) emptyList<Any?>() else error("'duzenler' eksik (${m["id"]})")
        val parsedTunings = rawTunings.map { t ->
            @Suppress("UNCHECKED_CAST")
            t as Map<String, Any?>
            val strings = (t["teller"] as List<*>).map { s ->
                @Suppress("UNCHECKED_CAST")
                s as Map<String, Any?>
                StringSpec(
                    label = str(s, "ad"),
                    note = str(s, "nota"),
                    count = (s["telSayisi"] as? Double)?.toInt()?.also { require(it in 1..4) { "telSayisi 1–4 olmalı" } },
                    octaveNotes = (s["oktavTelleri"] as? List<*>)?.map { it as String } ?: emptyList(),
                )
            }
            require(strings.isNotEmpty()) { "Telsiz düzen: ${t["id"]}" }
            TuningSpec(
                id = str(t, "id"),
                name = str(t, "ad"),
                strings = strings,
                source = t["kaynak"] as? String,
                verified = t["dogrulandi"] as? Boolean ?: false,
                note = t["not"] as? String,
            )
        }
        val tunings = if (free) listOf(TuningSpec("kromatik", "Kromatik", emptyList(), null, true, null)) else parsedTunings
        require(tunings.isNotEmpty()) { "Düzen yok: ${m["id"]}" }
        require(tunings.map { it.id }.toSet().size == tunings.size) { "Yinelenen düzen kimliği: ${m["id"]}" }

        val karar = (m["karar"] as? Map<*, *>)?.let { k ->
            KararSpec(
                written = k["yazili"] as String,
                default = k["varsayilan"] as String,
                options = (k["secenekler"] as List<*>).map { it as String },
            ).also { spec ->
                KararSpec.pitchClass(spec.written)
                spec.options.forEach { KararSpec.pitchClass(it) }
                require(spec.default in spec.options) { "Varsayılan karar seçeneklerde yok" }
            }
        }

        val transpositions = (m["transpozisyonlar"] as? List<*>)?.map { t ->
            t as Map<*, *>
            TranspositionOption(t["ad"] as String, (t["cent"] as Double))
        }?.also { require(it.isNotEmpty()) { "Boş transpozisyon listesi" } }
        require(karar == null || transpositions == null) { "Karar ve transpozisyon birlikte olamaz: ${m["id"]}" }

        val window = (m["pencere"] as? Double)?.toInt() ?: 2048
        require(window == 2048 || window == 4096) { "Pencere 2048 veya 4096 olmalı" }

        return InstrumentProfile(
            id = str(m, "id"),
            name = str(m, "ad"),
            family = str(m, "aile"),
            defaultReading = when (m["varsayilanOkuma"]) {
                "makam" -> ReadingMode.MAKAM
                else -> ReadingMode.WESTERN
            },
            window = window,
            octaveAgnostic = m["oktavdanBagimsiz"] as? Boolean ?: false,
            karar = karar,
            tunings = tunings,
            free = free,
            transpositions = transpositions,
            info = m["bilgi"] as? String,
        )
    }
}
