package com.family.dawa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenSurface,
    onPrimaryContainer = GreenPrimary,
    secondary = AmberPrimary,
    onSecondary = Color.White,
    secondaryContainer = AmberSurface,
    onSecondaryContainer = AmberPrimary,
    error = RedPrimary,
    onError = Color.White,
    errorContainer = RedSurface,
    onErrorContainer = RedPrimary,
    background = BgCalm,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary
)

@Composable
fun DawaTheme(content: @Composable () -> Unit) {
    // Force RTL for Arabic Elderly Caregiver
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = DawaTypography,
            content = content
        )
    }
}
