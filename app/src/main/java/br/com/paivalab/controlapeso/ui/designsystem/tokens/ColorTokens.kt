package br.com.paivalab.controlapeso.ui.designsystem.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ControlaPesoColors(
    val connected: Color,
    val disconnected: Color,
    val stable: Color,
    val measuring: Color,
    val informational: Color,
    val trendUp: Color,
    val trendDown: Color,
    val trendNeutral: Color,
    val chartPrimary: Color,
    val chartAverage: Color,
    val chartGrid: Color
)

fun ColorScheme.toControlaPesoColors(): ControlaPesoColors = ControlaPesoColors(
    connected = primary,
    disconnected = error,
    stable = primary,
    measuring = secondary,
    informational = secondary,
    trendUp = tertiary,
    trendDown = secondary,
    trendNeutral = onSurfaceVariant,
    chartPrimary = primary,
    chartAverage = tertiary,
    chartGrid = outlineVariant
)

