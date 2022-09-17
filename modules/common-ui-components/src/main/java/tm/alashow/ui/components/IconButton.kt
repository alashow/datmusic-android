/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.LocalContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.AppTheme

private val RippleRadius = 24.dp
private val IconButtonSizeModifier = Modifier.size(48.dp)

val IconRippleRadiusMedium = 32.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rippleColor: Color = Color.Unspecified,
    rippleRadius: Dp = RippleRadius,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, color = rippleColor, radius = rippleRadius)
            )
            .then(IconButtonSizeModifier),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        ProvideContentAlpha(contentAlpha, content = content)
    }
}

fun Modifier.textIconModifier() = composed {
    Modifier
        .size(24.dp)
        .padding(end = AppTheme.specs.paddingTiny)
}
