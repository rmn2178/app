package com.example.monotodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MonoPalette {
    val PureBlack = Color(0xFF000000)
    val Obsidian = Color(0xFF0E0E10)
    val Charcoal = Color(0xFF1F1F23)
    val Graphite = Color(0xFF3E3E44)
    val SlateGray = Color(0xFF787880)
    val Silver = Color(0xFFC8C8CE)
    val Mist = Color(0xFFECECEF)
    val OffWhite = Color(0xFFF7F7F9)
    val PureWhite = Color(0xFFFFFFFF)
}

private val MonochromeColorScheme = darkColorScheme(
    primary = MonoPalette.PureWhite,
    onPrimary = MonoPalette.PureBlack,
    primaryContainer = MonoPalette.Charcoal,
    onPrimaryContainer = MonoPalette.PureWhite,
    secondary = MonoPalette.Silver,
    onSecondary = MonoPalette.PureBlack,
    background = MonoPalette.PureBlack,
    onBackground = MonoPalette.PureWhite,
    surface = MonoPalette.Obsidian,
    onSurface = MonoPalette.PureWhite,
    surfaceVariant = MonoPalette.Charcoal,
    onSurfaceVariant = MonoPalette.Silver,
    outline = MonoPalette.Graphite
)

@Composable
fun MonoTodoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        content = content
    )
}
