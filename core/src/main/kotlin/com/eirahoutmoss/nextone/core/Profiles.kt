package com.eirahoutmoss.nextone.core

import com.eirahoutmoss.nextone.tuning.Notes

enum class ReadingMode { WESTERN, MAKAM }

data class StringSpec(val label: String, val note: String) {
    val midi: Int = Notes.parse(note)
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

data class InstrumentProfile(
    val id: String,
    val name: String,
    val family: String,
    val defaultReading: ReadingMode,
    val window: Int,
    val octaveAgnostic: Boolean,
    val karar: KararSpec?,
    val tunings: List<TuningSpec>,
) {
    fun tuning(id: String?): TuningSpec = tunings.firstOrNull { it.id == id } ?: tunings.first()
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

        val tunings = (m["duzenler"] as? List<*> ?: error("'duzenler' eksik")).map { t ->
            @Suppress("UNCHECKED_CAST")
            t as Map<String, Any?>
            val strings = (t["teller"] as List<*>).map { s ->
                @Suppress("UNCHECKED_CAST")
                s as Map<String, Any?>
                StringSpec(str(s, "ad"), str(s, "nota"))
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
        )
    }
}
