package br.com.paivalab.controlapeso.ui.designsystem.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

@Immutable
data class ControlaPesoMotion(
    val immediateMillis: Int = 0,
    val shortMillis: Int = 150,
    val mediumMillis: Int = 250,
    val emphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
)

