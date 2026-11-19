package com.example.monotodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MonoPalette {
    val PureBlack = Color(0xFF000000)
    val Obsidian = Color(0xFF0C0C0E)
    val Charcoal = Color(0xFF1C1C20)
    val Graphite = Color(0xFF3A3A40)
    val SlateGray = Color(0xFF74747C)
    val Silver = Color(0xFFC4C4CC)
    val Mist = Color(0xFFEBEBEF)
    val OffWhite = Color(0xFFF6F6F8)
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
