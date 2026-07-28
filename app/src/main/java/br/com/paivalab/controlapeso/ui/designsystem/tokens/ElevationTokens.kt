package br.com.paivalab.controlapeso.ui.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ControlaPesoElevation(
    val resting: Dp = 0.dp,
    val raised: Dp = 1.dp,
    val floating: Dp = 3.dp
)

