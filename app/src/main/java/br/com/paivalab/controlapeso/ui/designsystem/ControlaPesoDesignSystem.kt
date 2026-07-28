package br.com.paivalab.controlapeso.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoColors
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoElevation
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoMotion
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoSizes
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoSpacing
import br.com.paivalab.controlapeso.ui.designsystem.tokens.toControlaPesoColors

private val LocalControlaPesoColors = staticCompositionLocalOf<ControlaPesoColors> {
    error("ControlaPeso colors were not provided")
}
private val LocalControlaPesoSpacing = staticCompositionLocalOf { ControlaPesoSpacing() }
private val LocalControlaPesoSizes = staticCompositionLocalOf { ControlaPesoSizes() }
private val LocalControlaPesoElevation = staticCompositionLocalOf {
    ControlaPesoElevation()
}
private val LocalControlaPesoMotion = staticCompositionLocalOf { ControlaPesoMotion() }

object ControlaPesoDesignSystem {
    val colors: ControlaPesoColors
        @Composable
        @ReadOnlyComposable
        get() = LocalControlaPesoColors.current

    val spacing: ControlaPesoSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalControlaPesoSpacing.current

    val sizes: ControlaPesoSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalControlaPesoSizes.current

    val elevation: ControlaPesoElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalControlaPesoElevation.current

    val motion: ControlaPesoMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalControlaPesoMotion.current
}

@Composable
fun ProvideControlaPesoDesignSystem(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalControlaPesoColors provides MaterialTheme.colorScheme.toControlaPesoColors(),
        LocalControlaPesoSpacing provides ControlaPesoSpacing(),
        LocalControlaPesoSizes provides ControlaPesoSizes(),
        LocalControlaPesoElevation provides ControlaPesoElevation(),
        LocalControlaPesoMotion provides ControlaPesoMotion(),
        content = content
    )
}

