package br.com.paivalab.controlapeso.ui.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ControlaPesoSizes(
    val minimumTouchTarget: Dp = 48.dp,
    val icon: Dp = 24.dp,
    val compactContentPadding: Dp = 16.dp,
    val mediumContentPadding: Dp = 24.dp,
    val expandedContentPadding: Dp = 32.dp,
    val contentMaxWidth: Dp = 1_200.dp
)

enum class ControlaPesoWindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

fun controlaPesoWindowSize(width: Dp): ControlaPesoWindowSize = when {
    width >= 840.dp -> ControlaPesoWindowSize.EXPANDED
    width >= 600.dp -> ControlaPesoWindowSize.MEDIUM
    else -> ControlaPesoWindowSize.COMPACT
}
