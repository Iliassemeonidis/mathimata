package com.example.androidtutorial.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BookColorScheme = lightColorScheme(
    primary = BookColors.Primary,
    onPrimary = Color.White,
    background = BookColors.Paper,
    onBackground = BookColors.Ink,
    surface = BookColors.Paper,
    onSurface = BookColors.Ink,
    onSurfaceVariant = BookColors.InkSecondary,
)

@Composable
fun MathimataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookColorScheme,
        typography = Typography,
        content = content,
    )
}
