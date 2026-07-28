package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoWindowSize
import br.com.paivalab.controlapeso.ui.designsystem.tokens.controlaPesoWindowSize

@Composable
fun AdaptiveContentPane(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(ControlaPesoWindowSize, PaddingValues) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val windowSize = controlaPesoWindowSize(maxWidth)
        val horizontalPadding = when (windowSize) {
            ControlaPesoWindowSize.COMPACT ->
                ControlaPesoDesignSystem.sizes.compactContentPadding
            ControlaPesoWindowSize.MEDIUM ->
                ControlaPesoDesignSystem.sizes.mediumContentPadding
            ControlaPesoWindowSize.EXPANDED ->
                ControlaPesoDesignSystem.sizes.expandedContentPadding
        }
        val padding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = ControlaPesoDesignSystem.spacing.lg
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = ControlaPesoDesignSystem.sizes.contentMaxWidth)
                .padding(padding)
        ) {
            content(windowSize, PaddingValues())
        }
    }
}
