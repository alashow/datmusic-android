/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.simpleClickable(
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit,
) = composed {
    clickable(
        onClick = onClick,
        role = Role.Button,
        indication = indication,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    )
}

fun Modifier.coloredRippleClickable(
    onClick: () -> Unit,
    color: Color? = null,
    bounded: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    rippleRadius: Dp = 24.dp,
) = composed {
    clickable(
        onClick = onClick,
        role = Role.Button,
        indication = rememberRipple(color = color ?: MaterialTheme.colorScheme.secondary, bounded = bounded, radius = rippleRadius),
        interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    )
}
