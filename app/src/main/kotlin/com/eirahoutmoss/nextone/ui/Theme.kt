package com.eirahoutmoss.nextone.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Nex ailesi renkleri (NexHub marka kolajından ölçülen değerler). */
object NexColors {
    val Cyan = Color(0xFF00CCFC)
    val Blue = Color(0xFF1A75FF)
    val Violet = Color(0xFF7A38F5)
    val Black = Color(0xFF020710)
    val InTune = Color(0xFF22C55E)
    val Sharp = Color(0xFFF59E0B)
}

private val Dark = darkColorScheme(
    primary = NexColors.Cyan,
    secondary = NexColors.Violet,
    tertiary = NexColors.Blue,
    background = NexColors.Black,
    surface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFF16203A),
    onPrimary = NexColors.Black,
    onBackground = Color(0xFFFDFCFC),
    onSurface = Color(0xFFFDFCFC),
    onSurfaceVariant = Color(0xFFB4BCD0),
)

private val Light = lightColorScheme(
    primary = NexColors.Blue,
    secondary = NexColors.Violet,
    tertiary = NexColors.Cyan,
    background = Color(0xFFF6F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EBF5),
    onPrimary = Color.White,
    onBackground = Color(0xFF0B1220),
    onSurface = Color(0xFF0B1220),
    onSurfaceVariant = Color(0xFF4A556C),
)

@Composable
fun NexToneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
