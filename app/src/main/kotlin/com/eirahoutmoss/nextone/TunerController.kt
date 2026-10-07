package com.eirahoutmoss.nextone

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eirahoutmoss.nextone.core.ClassCode
import com.eirahoutmoss.nextone.core.InstrumentProfile
import com.eirahoutmoss.nextone.core.Profiles
import com.eirahoutmoss.nextone.core.ReadingMode
import com.eirahoutmoss.nextone.core.TunerSession
import com.eirahoutmoss.nextone.core.TunerSettings
import com.eirahoutmoss.nextone.core.TunerView
import com.eirahoutmoss.nextone.dsp.TrackerState

enum class MicPermission { UNKNOWN, GRANTED, DENIED }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Arayüz durumunun tek sahibi. Ses motorundan gelen takipçi durumlarını ana iş parçacığında
 * [TunerSession]'a verir ve sonucu Compose durumuna yazar. Seçimleri SharedPreferences'ta saklar.
 */
class TunerController(private val context: Context) {
    private val prefs = context.getSharedPreferences("nextone", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())

    val profiles: List<InstrumentProfile> = Profiles.loadAll()

    var profile by mutableStateOf(profiles.firstOrNull { it.id == prefs.getString("profile", null) } ?: profiles.first())
        private set
    var tuningId by mutableStateOf(prefs.getString("tuning_${profile.id}", null) ?: profile.tunings.first().id)
        private set
    /** Karar sesi (bağlama) ya da transpozisyon (serbest mod); seçeneği olmayan çalgıda null. */
    var option by mutableStateOf(loadOption(profile))
        private set
    var settings by mutableStateOf(loadSettings())
        private set
    /** Etkin sınıf kodu; varken La4, (isteğe bağlı) perde sistemi ve bağlama karar sesi koddan gelir. */
    var classCode by mutableStateOf(ClassCode.parse(prefs.getString("classCode", null) ?: ""))
        private set
    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!) }.getOrDefault(ThemeMode.SYSTEM)
    )
        private set
    var session by mutableStateOf(newSession())
        private set
    var view by mutableStateOf(session.update(TrackerState.Silence, 0))
        private set
    var permission by mutableStateOf(MicPermission.UNKNOWN)
    var error by mutableStateOf<String?>(null)
        private set

    private val engine = AudioEngine(
        context,
        onState = { state, now -> main.post { onTracker(state, now) } },
        onError = { msg -> main.post { error = msg } },
    )
    private var engineWindow = 0

    // ---- Yaşam döngüsü ----

    fun start() {
        if (permission != MicPermission.GRANTED) return
        error = null
        if (engine.isRunning && engineWindow == profile.window) return
        engine.stop()
        engineWindow = profile.window
        engine.start(profile.window)
    }

    fun stop() {
        engine.stop()
    }

    private fun onTracker(state: TrackerState, now: Long) {
        val v = session.update(state, now)
        view = v
        if (v.signal?.confirmedNow == true) vibrate()
    }

    // ---- Kullanıcı seçimleri ----

    fun selectProfile(p: InstrumentProfile) {
        profile = p
        tuningId = prefs.getString("tuning_${p.id}", null) ?: p.tunings.first().id
        option = loadOption(p)
        prefs.edit().putString("profile", p.id).apply()
        rebuild()
        if (engine.isRunning && engineWindow != p.window) start()
    }

    fun selectTuning(id: String) {
        tuningId = id
        prefs.edit().putString("tuning_${profile.id}", id).apply()
        rebuild()
    }

    fun selectOption(o: String) {
        option = o
        prefs.edit().putString("karar_${profile.id}", o).apply()
        rebuild()
    }

    fun updateSettings(s: TunerSettings) {
        settings = s
        prefs.edit()
            .putFloat("a4", s.a4.toFloat())
            .putString("reading", when (s.readingOverride) { null -> "auto"; ReadingMode.WESTERN -> "bati"; ReadingMode.MAKAM -> "makam" })
            .putString("makamSystem", s.makamSystemId)
            .putFloat("zone", s.greenZoneCents.toFloat())
            .apply()
        rebuild()
    }

    fun applyClassCode(code: ClassCode) {
        classCode = code
        prefs.edit().putString("classCode", code.text).apply()
        rebuild()
    }

    fun leaveClassCode() {
        classCode = null
        prefs.edit().remove("classCode").apply()
        rebuild()
    }

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    /** Sınıf kodu bağlama ailesinin karar sesini belirliyorsa true. */
    val optionLockedByClass: Boolean
        get() = classCode != null && profile.karar != null

    /** null: otomatik tel tanımaya dön. */
    fun selectString(index: Int?) {
        session.selectString(index)
        view = session.update(TrackerState.Unclear, SystemClock.elapsedRealtime())
    }

    fun playReference() {
        if (view.free) {
            // Serbest mod: son duyulan sese en yakın perde; henüz ses yoksa La4
            ReferenceTone.play(view.signal?.heardTargetHz ?: settings.a4)
            return
        }
        val idx = view.activeString.takeIf { it >= 0 } ?: 0
        val target = view.strings.getOrNull(idx) ?: return
        ReferenceTone.play(target.targetHz)
    }

    private fun rebuild() {
        session = newSession()
        view = session.update(TrackerState.Silence, SystemClock.elapsedRealtime())
    }

    private fun newSession(): TunerSession {
        val code = classCode
        val effective = if (code == null) settings else settings.copy(
            a4 = code.a4.toDouble(),
            makamSystemId = if (code.koma53) "koma53" else settings.makamSystemId,
        )
        val effectiveOption = if (code != null && profile.karar != null) code.karar else option
        return TunerSession(profile, profile.tuning(tuningId), effective, effectiveOption)
    }

    private fun loadOption(p: InstrumentProfile): String? =
        prefs.getString("karar_${p.id}", null)?.takeIf { it in p.optionNames } ?: p.defaultOption

    private fun loadSettings() = TunerSettings(
        a4 = prefs.getFloat("a4", 440f).toDouble(),
        readingOverride = when (prefs.getString("reading", "auto")) {
            "bati" -> ReadingMode.WESTERN
            "makam" -> ReadingMode.MAKAM
            else -> null
        },
        makamSystemId = prefs.getString("makamSystem", "aeu") ?: "aeu",
        greenZoneCents = prefs.getFloat("zone", 3f).toDouble(),
    )

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        else v.vibrate(60)
    }
}
