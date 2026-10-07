package com.eirahoutmoss.nextone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eirahoutmoss.nextone.MicPermission
import com.eirahoutmoss.nextone.TunerController
import com.eirahoutmoss.nextone.core.ReadingMode
import com.eirahoutmoss.nextone.core.TunerView
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val TR = Locale("tr", "TR")

private fun fmt(value: Double, pattern: String) = String.format(TR, pattern, value)

@Composable
fun TunerScreen(
    controller: TunerController,
    version: String,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            TopBar(controller, onSettings = { showSettings = true })
            Spacer(Modifier.height(8.dp))

            when (controller.permission) {
                MicPermission.DENIED -> PermissionNeeded(onRequestPermission, onOpenAppSettings)
                else -> TunerBody(controller)
            }
        }
    }

    if (showSettings) {
        SettingsDialog(controller, version, onDismiss = { showSettings = false })
    }
}

// ---------------------------------------------------------------- Üst çubuk

@Composable
private fun TopBar(controller: TunerController, onSettings: () -> Unit) {
    val profile = controller.profile
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Picker(
            label = profile.name,
            options = controller.profiles.map { it.name },
            onPick = { controller.selectProfile(controller.profiles[it]) },
            emphasized = true,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSettings) { Text("Ayarlar", fontSize = 15.sp) }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        val tuning = profile.tuning(controller.tuningId)
        Picker(
            label = tuning.name,
            options = profile.tunings.map { it.name },
            onPick = { controller.selectTuning(profile.tunings[it].id) },
        )
        val karar = profile.karar
        if (karar != null) {
            Spacer(Modifier.width(4.dp))
            Picker(
                label = "Karar: ${controller.karar}",
                options = karar.options,
                onPick = { controller.selectKarar(karar.options[it]) },
            )
        }
    }
}

@Composable
private fun Picker(label: String, options: List<String>, onPick: (Int) -> Unit, emphasized: Boolean = false) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Text(
                "$label ▾",
                fontSize = if (emphasized) 22.sp else 16.sp,
                fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { i, o ->
                DropdownMenuItem(text = { Text(o, fontSize = 17.sp) }, onClick = { open = false; onPick(i) })
            }
        }
    }
}

// ---------------------------------------------------------------- Akort gövdesi

@Composable
private fun TunerBody(controller: TunerController) {
    val view = controller.view
    val session = controller.session
    val signal = view.signal
    val zone = controller.settings.greenZoneCents
    val active = view.strings.getOrNull(view.activeString)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
    ) {
        val tuning = controller.profile.tuning(controller.tuningId)
        if (!tuning.verified) {
            Text(
                "Bu düzen henüz bir öğretmen tarafından doğrulanmadı.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        controller.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))

        // Hedef tel adı (büyük)
        Text(
            text = active?.targetName ?: "—",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                signal == null -> MaterialTheme.colorScheme.onSurfaceVariant
                signal.inZone -> NexColors.InTune
                else -> MaterialTheme.colorScheme.onBackground
            },
        )
        Text(
            text = active?.let { "${it.label} tel" } ?: " ",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        Gauge(deviation = signal?.deviationCents, zoneCents = zone)

        // Sayısal sapma ve yön
        Text(
            text = signal?.let { centsText(it.deviationCents, session.readingMode, it) } ?: " ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = statusText(signal, zone),
            fontSize = 17.sp,
            color = when {
                signal == null -> MaterialTheme.colorScheme.onSurfaceVariant
                signal.confirmed -> NexColors.InTune
                signal.inZone -> NexColors.InTune
                else -> NexColors.Sharp
            },
        )

        Spacer(Modifier.height(8.dp))
        // Çalınan sesin okunuşu (ikincil)
        Text(
            text = signal?.let { "Duyulan: ${it.heardName}  ·  ${it.heardAltName}" } ?: "Bir tel çalın",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        StringButtons(view, onSelect = { idx ->
            controller.selectString(if (view.manual && view.activeString == idx) null else idx)
        })
        Text(
            if (view.manual) "Elle seçildi — otomatik tanımaya dönmek için aynı tele tekrar dokunun"
            else "Otomatik tel tanıma",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = { controller.playReference() }) {
            Text("♪  Referans sesi: ${active?.targetName ?: view.strings.firstOrNull()?.targetName ?: ""}", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
        val s = controller.settings
        Text(
            "La4 = ${fmt(s.a4, "%.0f")} Hz  ·  ${session.system.displayName}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun centsText(dev: Double, mode: ReadingMode, signal: TunerView.Signal): String {
    val c = fmt(dev, "%+.1f") + " cent"
    if (mode == ReadingMode.WESTERN) return c
    val koma = dev / (1200.0 / 53.0)
    return c + "  (" + fmt(koma, "%+.2f") + " koma)"
}

private fun statusText(signal: TunerView.Signal?, zone: Double): String = when {
    signal == null -> "Dinleniyor…"
    signal.confirmed -> "Akortlu ✓"
    signal.inZone -> "Tamam, sabit tutun…"
    abs(signal.deviationCents) > 50 -> {
        val semis = (signal.deviationCents / 100.0).roundToInt()
        if (signal.deviationCents > 0) "Çok tiz (≈ $semis yarım ton) — gevşetin" else "Çok pes (≈ ${-semis} yarım ton) — gerin"
    }
    signal.deviationCents > zone -> "Tiz — biraz gevşetin"
    else -> "Pes — biraz gerin"
}

@Composable
private fun StringButtons(view: TunerView, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        view.strings.forEachIndexed { i, s ->
            val isActive = i == view.activeString
            val colors = if (isActive) ButtonDefaults.buttonColors()
            else ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
            Button(
                onClick = { onSelect(i) },
                colors = colors,
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                modifier = Modifier.weight(1f).height(64.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.targetName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(s.label, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- İzin

@Composable
private fun PermissionNeeded(onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text("Mikrofon izni gerekli", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Text(
            "NexTone çalgınızın sesini duyabilmek için mikrofonu kullanır. " +
                "Ses kaydedilmez, saklanmaz ve hiçbir yere gönderilmez; uygulamanın internet izni yoktur.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequest) { Text("İzin ver") }
        TextButton(onClick = onOpenSettings) { Text("Uygulama ayarlarını aç") }
    }
}

// ---------------------------------------------------------------- Ayarlar

@Composable
private fun SettingsDialog(controller: TunerController, version: String, onDismiss: () -> Unit) {
    val s = controller.settings
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam") } },
        title = { Text("Ayarlar") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Referans frekansı (La4)", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { controller.updateSettings(s.copy(a4 = (s.a4 - 1).coerceAtLeast(415.0))) }) { Text("−", fontSize = 22.sp) }
                    Text("${fmt(s.a4, "%.0f")} Hz", fontSize = 20.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.Center)
                    TextButton(onClick = { controller.updateSettings(s.copy(a4 = (s.a4 + 1).coerceAtMost(466.0))) }) { Text("+", fontSize = 22.sp) }
                    TextButton(onClick = { controller.updateSettings(s.copy(a4 = 440.0)) }) { Text("440") }
                }

                Spacer(Modifier.height(12.dp))
                Text("Okuma", fontWeight = FontWeight.SemiBold)
                Choice("Çalgıya göre", s.readingOverride == null) { controller.updateSettings(s.copy(readingOverride = null)) }
                Choice("Batı (Do, Re, Mi…)", s.readingOverride == ReadingMode.WESTERN) { controller.updateSettings(s.copy(readingOverride = ReadingMode.WESTERN)) }
                Choice("Makam (Rast, Dügâh…)", s.readingOverride == ReadingMode.MAKAM) { controller.updateSettings(s.copy(readingOverride = ReadingMode.MAKAM)) }

                Spacer(Modifier.height(12.dp))
                Text("Makam perde sistemi", fontWeight = FontWeight.SemiBold)
                Choice("Arel-Ezgi-Uzdilek", s.makamSystemId == "aeu") { controller.updateSettings(s.copy(makamSystemId = "aeu")) }
                Choice("53 koma", s.makamSystemId == "koma53") { controller.updateSettings(s.copy(makamSystemId = "koma53")) }

                Spacer(Modifier.height(16.dp))
                Text("NexTone $version", fontWeight = FontWeight.SemiBold)
                Text(
                    "Ticari değildir. Yalnızca mikrofonu kullanır; internet izni yoktur, hiçbir veri cihazdan çıkmaz.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick).padding(vertical = 2.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, fontSize = 16.sp)
    }
}
