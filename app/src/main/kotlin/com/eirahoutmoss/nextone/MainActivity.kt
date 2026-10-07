package com.eirahoutmoss.nextone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.eirahoutmoss.nextone.ui.NexColors
import com.eirahoutmoss.nextone.ui.NexToneTheme
import com.eirahoutmoss.nextone.ui.TunerScreen

class MainActivity : ComponentActivity() {

    private lateinit var controller: TunerController
    private var askedThisSession = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            controller.permission = if (granted) MicPermission.GRANTED else MicPermission.DENIED
            if (granted) controller.start()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = TunerController(applicationContext)
        // Akort sırasında ekran kapanmasın
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (_: Exception) { "" }
        setContent {
            val dark = when (controller.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Durum çubuğu seçilen temaya uysun
            SideEffect {
                @Suppress("DEPRECATION")
                window.statusBarColor = if (dark) NexColors.Black.toArgb() else android.graphics.Color.rgb(0xF6, 0xF8, 0xFC)
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !dark
            }
            NexToneTheme(dark = dark) {
                TunerScreen(
                    controller = controller,
                    version = version,
                    onRequestPermission = { requestMic() },
                    onOpenAppSettings = { openAppSettings() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasMic()) {
            controller.permission = MicPermission.GRANTED
            controller.start()
        } else if (!askedThisSession) {
            requestMic()
        }
    }

    override fun onPause() {
        super.onPause()
        controller.stop()
        ReferenceTone.stop()
    }

    private fun hasMic() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestMic() {
        askedThisSession = true
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
