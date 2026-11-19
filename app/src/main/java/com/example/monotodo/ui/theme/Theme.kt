package com.example.monotodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MonoPalette {
    val PureBlack = Color(0xFF000000)
    val Obsidian = Color(0xFF0E0E11)
    val Charcoal = Color(0xFF1C1C20)
    val Graphite = Color(0xFF3E3E45)
    val MutedGray = Color(0xFF7A7A84)
    val Silver = Color(0xFFC4C4CC)
    val Hairline = Color(0xFFE6E6EB)
    val SoftSurface = Color(0xFFF3F3F6)
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
