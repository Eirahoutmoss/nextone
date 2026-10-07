package com.eirahoutmoss.nextone.tuning

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Bir sesin, seçili perde sistemine göre okunuşu.
 *
 * @param name birincil ad: Batı sisteminde "La4", makam sistemlerinde "Dügâh"
 * @param western en yakın Batı notası (her sistemde ikincil bilgi olarak gösterilir)
 * @param cents hedef perdeden sapma (+ tiz, − pes)
 * @param koma makam sistemlerinde sapmanın Holder koması (≈22,64 cent) karşılığı; Batı'da null
 * @param targetHz hedef perdenin GERÇEK (çalınan) frekansı
 */
data class Reading(
    val name: String,
    val western: String,
    val cents: Double,
    val koma: Double?,
    val targetHz: Double,
)

/**
 * Perde sistemi. Tüm sistemler aynı sözleşmeyi kullanır:
 *  - [a4]: referans frekansı (La4 = Dügâh). Kullanıcı ayarı, varsayılan 440 Hz.
 *  - [transposeCents]: gerçek ses = yazılı nota + transpozisyon. Bağlamanın karar sesi,
 *    ud/ney ahengi ve Si♭/Mi♭ üflemeliler bu tek değerle ifade edilir.
 * Okuma her zaman YAZILI notaya göre yapılır; hedef frekans ise gerçek sese çevrilerek döner.
 */
interface PitchSystem {
    val id: String
    val displayName: String
    fun read(soundingHz: Double, a4: Double = 440.0, transposeCents: Double = 0.0): Reading
}

private fun log2(x: Double) = ln(x) / ln(2.0)

/** 12 eşit ses (Batı). */
object EqualTemperament : PitchSystem {
    override val id = "12tet"
    override val displayName = "12 eşit ses (Batı)"

    override fun read(soundingHz: Double, a4: Double, transposeCents: Double): Reading {
        val written = 1200.0 * log2(soundingHz / a4) - transposeCents   // La4'e göre cent
        val semis = (written / 100.0).roundToInt()
        val target = a4 * 2.0.pow((semis * 100.0 + transposeCents) / 1200.0)
        val n = Notes.name(69 + semis)
        return Reading(n, n, written - semis * 100.0, null, target)
    }
}

/**
 * Makam perde sistemi: Rast'tan (yazılı Sol4) itibaren bir oktavlık perde listesi.
 * Dügâh (yazılı La4) referans frekansına [a4] sabitlenir; tüm perdeler ondan türetilir.
 */
class MakamSystem(
    override val id: String,
    override val displayName: String,
    /** Rast'tan cent cinsinden uzaklıklar, artan sırada, 0 ile başlar, 1200 içermez. */
    private val degrees: DoubleArray,
    private val dugahCents: Double,
) : PitchSystem {

    init {
        require(degrees.size == MIDDLE.size) { "Perde sayısı ${MIDDLE.size} olmalı" }
        require(degrees[0] == 0.0)
    }

    override fun read(soundingHz: Double, a4: Double, transposeCents: Double): Reading {
        val writtenFromA4 = 1200.0 * log2(soundingHz / a4) - transposeCents
        val fromRast4 = writtenFromA4 + dugahCents
        var octave = floor(fromRast4 / 1200.0).toInt()
        val within = fromRast4 - octave * 1200.0
        var best = 0
        var bestDist = Double.MAX_VALUE
        for (i in degrees.indices) {
            val d = abs(within - degrees[i])
            if (d < bestDist) { bestDist = d; best = i }
        }
        if (abs(within - 1200.0) < bestDist) { best = 0; octave += 1 }
        val targetFromRast4 = octave * 1200.0 + degrees[best]
        val dev = fromRast4 - targetFromRast4
        val targetWrittenFromA4 = targetFromRast4 - dugahCents
        val target = a4 * 2.0.pow((targetWrittenFromA4 + transposeCents) / 1200.0)
        val westernSemis = (targetWrittenFromA4 / 100.0).roundToInt()
        val western = Notes.name(69 + westernSemis)
        return Reading(perdeName(best, octave, western), western, dev, dev / KOMA_CENTS, target)
    }

    companion object {
        /** Holder koması: oktavın 53'te biri. */
        const val KOMA_CENTS = 1200.0 / 53.0

        /** Orta oktav (Rast–Dik Mâhur) perde adları; AEU'nun 24 perdesinin 23'ü + Rast. */
        val MIDDLE = arrayOf(
            "Rast", "Nîm Zirgüle", "Zirgüle", "Dik Zirgüle", "Dügâh", "Kürdî", "Dik Kürdî",
            "Segâh", "Bûselik", "Çârgâh", "Nîm Hicaz", "Hicaz", "Dik Hicaz", "Nevâ",
            "Nîm Hisar", "Hisar", "Dik Hisar", "Hüseynî", "Acem", "Dik Acem", "Eviç", "Mâhur", "Dik Mâhur",
        )

        /** Pes oktavda özel adı olan perdeler (yazılı Sol3–Fa♯4 aralığı). [DOĞRULANACAK] */
        private val LOW = mapOf(
            "Nevâ" to "Yegâh",
            "Hüseynî" to "Hüseynî Aşîrân",
            "Acem" to "Acem Aşîrân",
            "Eviç" to "Irak",
            "Mâhur" to "Gevest",
        )

        /** Tiz oktavda özel adı olan perdeler (yazılı Sol5 ve üstü). [DOĞRULANACAK] */
        private val HIGH = mapOf(
            "Rast" to "Gerdaniye",
            "Dügâh" to "Muhayyer",
            "Kürdî" to "Sünbüle",
        )

        private fun perdeName(index: Int, octave: Int, western: String): String {
            val base = MIDDLE[index]
            return when (octave) {
                0 -> base
                -1 -> LOW[base] ?: "Kaba $base"
                1 -> HIGH[base] ?: "Tiz $base"
                else -> "$base ($western)"
            }
        }

        private fun pyth(num: Double, den: Double) = 1200.0 * log2(num / den)

        /**
         * Arel-Ezgi-Uzdilek: perdeler Pisagor oranlarından hesaplanır (elle yazılmış cent yok).
         * Örn. Dügâh 9/8 = 203,91 cent; Segâh 8192/6561 = 384,36 cent.
         */
        val AEU = MakamSystem(
            id = "aeu",
            displayName = "Arel-Ezgi-Uzdilek",
            degrees = doubleArrayOf(
                0.0,
                pyth(256.0, 243.0), pyth(2187.0, 2048.0), pyth(65536.0, 59049.0), pyth(9.0, 8.0),
                pyth(32.0, 27.0), pyth(19683.0, 16384.0), pyth(8192.0, 6561.0), pyth(81.0, 64.0),
                pyth(4.0, 3.0), pyth(1024.0, 729.0), pyth(729.0, 512.0), pyth(262144.0, 177147.0),
                pyth(3.0, 2.0), pyth(128.0, 81.0), pyth(6561.0, 4096.0), pyth(32768.0, 19683.0),
                pyth(27.0, 16.0), pyth(16.0, 9.0), pyth(59049.0, 32768.0), pyth(4096.0, 2187.0),
                pyth(243.0, 128.0), pyth(1048576.0, 531441.0),
            ),
            dugahCents = pyth(9.0, 8.0),
        )

        /** 53 koma (Holder): aynı perdelerin eşit bölmeli koma karşılıkları. */
        private val KOMA_STEPS = intArrayOf(
            0, 4, 5, 8, 9, 13, 14, 17, 18, 22, 26, 27, 30, 31, 35, 36, 39, 40, 44, 45, 48, 49, 52,
        )
        val KOMA53 = MakamSystem(
            id = "koma53",
            displayName = "53 koma",
            degrees = DoubleArray(KOMA_STEPS.size) { KOMA_STEPS[it] * KOMA_CENTS },
            dugahCents = 9 * KOMA_CENTS,
        )
    }
}

object PitchSystems {
    val ALL: List<PitchSystem> = listOf(EqualTemperament, MakamSystem.AEU, MakamSystem.KOMA53)
    fun byId(id: String) = ALL.first { it.id == id }
}
