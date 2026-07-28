package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.paivalab.controlapeso.ui.designsystem.ControlaPesoDesignSystem
import br.com.paivalab.controlapeso.ui.designsystem.tokens.ControlaPesoWindowSize
import br.com.paivalab.controlapeso.ui.designsystem.tokens.controlaPesoWindowSize

/**
 * Standard responsive container for scrollable root and secondary screens.
 *
 * It deliberately owns only layout and surface styling. Screen state and
 * actions remain in the calling feature.
 */
@Composable
fun ResponsiveScreenList(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 840.dp,
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val horizontalPadding = when (controlaPesoWindowSize(maxWidth)) {
            ControlaPesoWindowSize.COMPACT ->
                ControlaPesoDesignSystem.sizes.compactContentPadding
            ControlaPesoWindowSize.MEDIUM ->
                ControlaPesoDesignSystem.sizes.mediumContentPadding
            ControlaPesoWindowSize.EXPANDED ->
                ControlaPesoDesignSystem.sizes.expandedContentPadding
        }
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .widthIn(max = maxContentWidth),
            contentPadding = PaddingValues(
                horizontal = horizontalPadding,
                vertical = ControlaPesoDesignSystem.spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(
                ControlaPesoDesignSystem.spacing.md
            ),
            content = content
        )
    }
}
