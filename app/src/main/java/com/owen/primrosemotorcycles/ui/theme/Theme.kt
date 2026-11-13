package com.owen.primrosemotorcycles.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */






private val LightScheme = lightColorScheme(
    primary = PrimroseBlue,
    onPrimary = Color.White,
    primaryContainer = PrimroseBlueLight,
    onPrimaryContainer = PrimroseBlueDark,
    secondary = PrimroseGreen,
    onSecondary = Color.White,
    secondaryContainer = PrimroseMint,
    onSecondaryContainer = PrimroseGreenDark,
    tertiary = PrimroseTeal,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFE3F0EB),
    onSurfaceVariant = Color(0xFF4A5A6A),
    outline = Color(0xFFC5D6E0)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0A2F6B),
    primaryContainer = PrimroseBlueDark,
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF4ADE80),
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF0B3D2E),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF1E3A5F),
    outline = Color(0xFF2A4A6B)
)

@Composable
fun PrimroseMotorcyclesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primroseTypography = MaterialTheme.typography
    val primroseShapes = MaterialTheme.shapes
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = primroseTypography,
        shapes = primroseShapes,
        content = content
    )
}