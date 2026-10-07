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
import androidx.core.content.ContextCompat
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
            NexToneTheme {
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
