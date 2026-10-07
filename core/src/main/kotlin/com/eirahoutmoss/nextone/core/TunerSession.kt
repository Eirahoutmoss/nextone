package com.eirahoutmoss.nextone.core

import com.eirahoutmoss.nextone.dsp.TrackerState
import com.eirahoutmoss.nextone.tuning.EqualTemperament
import com.eirahoutmoss.nextone.tuning.MakamSystem
import com.eirahoutmoss.nextone.tuning.Notes
import com.eirahoutmoss.nextone.tuning.PitchSystem
import com.eirahoutmoss.nextone.tuning.StringMatcher
import kotlin.math.abs

/** Kullanıcı ayarları (tüm çalgılar için ortak). */
data class TunerSettings(
    val a4: Double = 440.0,
    /** null: çalgının varsayılanı. */
    val readingOverride: ReadingMode? = null,
    /** Makam okumasında kullanılacak sistem: "aeu" veya "koma53". */
    val makamSystemId: String = "aeu",
    /** "Akortlu" sayılan bölge, ± cent. */
    val greenZoneCents: Double = 3.0,
    /** Bölgede bu kadar kalınca "tamam" onayı verilir. */
    val confirmHoldMs: Long = 1000,
)

/** Arayüzdeki bir tel düğmesi. */
data class StringView(
    val label: String,
    val targetName: String,
    val targetHz: Double,
    /** Gruptaki tel sayısı; bilinmiyorsa null. */
    val count: Int? = null,
    /** Gruptaki oktav tellerinin adları (ör. "Mi3"). */
    val octaveNames: List<String> = emptyList(),
)

/** Ekranın tek bir anlık görüntüsü. */
data class TunerView(
    val strings: List<StringView>,
    val activeString: Int,
    val manual: Boolean,
    /** null: ses yok ya da net değil. */
    val signal: Signal?,
    val listening: Boolean,
    /** Serbest (kromatik) mod: tel yok; sapma en yakın perdeye göre. */
    val free: Boolean = false,
) {
    data class Signal(
        val frequency: Double,
        /** Aktif telin hedefinden sapma (cent). */
        val deviationCents: Double,
        val inZone: Boolean,
        /** Bu karede "tamam" onayı ilk kez verildi (titreşim için). */
        val confirmedNow: Boolean,
        /** Bölgede yeterince kalındı. */
        val confirmed: Boolean,
        /** Çalınan sesin okunuşu (seçili okuma diliyle). */
        val heardName: String,
        val heardCents: Double,
        /** Çalınan sesin diğer dildeki adı (ikincil satır). */
        val heardAltName: String,
        val heardKoma: Double?,
        /** Çalınan sese en yakın perdenin gerçek frekansı (serbest modda referans sesi için). */
        val heardTargetHz: Double,
    )
}

/**
 * Bir çalgı + düzen + ayar üçlüsü için akort mantığı. Ses motorundan gelen her takipçi
 * durumunu bir [TunerView]'e çevirir. Android'den bağımsızdır; tamamen test edilebilir.
 */
class TunerSession(
    val profile: InstrumentProfile,
    val tuning: TuningSpec,
    val settings: TunerSettings,
    /** Karar sesi (bağlama) ya da transpozisyon seçeneğinin adı; null: profilin varsayılanı. */
    option: String? = null,
) {
    val optionName: String? = option?.takeIf { it in profile.optionNames } ?: profile.defaultOption
    val transposeCents: Double = profile.transposeCents(optionName)
    val free: Boolean = profile.free || tuning.strings.isEmpty()
    val readingMode: ReadingMode = settings.readingOverride ?: profile.defaultReading
    val system: PitchSystem =
        if (readingMode == ReadingMode.WESTERN) EqualTemperament
        else if (settings.makamSystemId == "koma53") MakamSystem.KOMA53 else MakamSystem.AEU
    private val altSystem: PitchSystem =
        if (readingMode == ReadingMode.WESTERN) (if (settings.makamSystemId == "koma53") MakamSystem.KOMA53 else MakamSystem.AEU)
        else EqualTemperament

    private val octaveTargets: List<DoubleArray> = tuning.strings.map { s ->
        s.octaveMidis.map { system.target(it, settings.a4, transposeCents) }.toDoubleArray()
    }

    val strings: List<StringView> = tuning.strings.mapIndexed { i, s ->
        val hz = system.target(s.midi, settings.a4, transposeCents)
        StringView(
            label = s.label,
            targetName = targetName(s.midi, hz),
            targetHz = hz,
            count = s.count,
            octaveNames = s.octaveMidis.mapIndexed { j, m -> targetName(m, octaveTargets[i][j]) },
        )
    }

    private val matcher = StringMatcher(
        strings.map { it.targetHz }.toDoubleArray(),
        octaveAgnostic = profile.octaveAgnostic,
        extraTargetsHz = octaveTargets,
    )
    private var manualIndex: Int? = null
    private var zoneSince: Long = -1
    private var confirmed = false
    private var lastKey = ""

    fun selectString(index: Int?) {
        if (free) return
        manualIndex = index
        if (index != null) matcher.select(index)
        resetZone()
    }

    fun update(state: TrackerState, nowMs: Long): TunerView {
        if (state !is TrackerState.Pitch) {
            if (state is TrackerState.Silence) resetZone()
            return TunerView(strings, if (free) -1 else activeIndex(), manualIndex != null, null, listening = true, free = free)
        }
        val f = state.frequency
        val nf = nameFrequency(f)
        val heard = system.read(nf, settings.a4, transposeCents)
        val alt = altSystem.read(nf, settings.a4, transposeCents)

        val idx: Int
        val dev: Double
        if (free) {
            // Serbest mod: hedef, çalınan sese en yakın perde
            idx = -1
            dev = heard.cents
            if (heard.name != lastKey) { resetZone(); lastKey = heard.name }
        } else {
            idx = manualIndex ?: matcher.match(f)
            dev = matcher.deviation(f, idx)
            if ("$idx" != lastKey) { resetZone(); lastKey = "$idx" }
        }
        val zone = settings.greenZoneCents
        // Bölgeden çıkış için iki kat geniş eşik: sınırda titrerken onay tekrar tekrar verilmesin
        val inZone = abs(dev) <= zone || (zoneSince >= 0 && abs(dev) <= 2 * zone)
        var confirmedNow = false
        if (inZone && state.settled) {
            if (zoneSince < 0) zoneSince = nowMs
            if (!confirmed && nowMs - zoneSince >= settings.confirmHoldMs) {
                confirmed = true
                confirmedNow = true
            }
        } else if (!inZone) {
            resetZone()
        }
        val signal = TunerView.Signal(
            frequency = f,
            deviationCents = dev,
            inZone = abs(dev) <= zone,
            confirmedNow = confirmedNow,
            confirmed = confirmed,
            heardName = displayName(heard.name),
            heardCents = heard.cents,
            heardAltName = displayName(alt.name),
            heardKoma = heard.koma,
            heardTargetHz = heard.targetHz * (f / nf),
        )
        return TunerView(strings, idx, manualIndex != null && !free, signal, listening = true, free = free)
    }

    private fun activeIndex() = manualIndex ?: matcher.current

    private fun resetZone() {
        zoneSince = -1
        confirmed = false
    }

    private fun targetName(writtenMidi: Int, targetHz: Double): String {
        val name = if (readingMode == ReadingMode.WESTERN) Notes.name(writtenMidi)
        else system.read(nameFrequency(targetHz), settings.a4, transposeCents).name
        return displayName(name)
    }

    /**
     * Oktavdan bağımsız çalgılarda adlandırma için sesi orta oktava (yazılı Sol4–Fa♯5,
     * yani Rast–Eviç) katlar; böylece bağlama telleri Dügâh, Nevâ, Rast diye adlanır.
     * Diğer çalgılarda frekansı değiştirmez.
     */
    private fun nameFrequency(f: Double): Double {
        if (!profile.octaveAgnostic) return f
        var written = Notes.cents(f, settings.a4) - transposeCents
        var out = f
        while (written < -250.0) { written += 1200.0; out *= 2.0 }
        while (written >= 950.0) { written -= 1200.0; out /= 2.0 }
        return out
    }

    /** Oktavdan bağımsız çalgılarda (bağlama) Batı adlarındaki oktav numarası gösterilmez. */
    private fun displayName(name: String): String =
        if (profile.octaveAgnostic) name.replace(Regex("""-?\d+$"""), "").replace(Regex("""\s*\(.*\)$"""), "") else name
}
