/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacer that has a height of a software keyboard
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeyboardSpacer(
    modifier: Modifier = Modifier,
    confirmHeight: (Dp) -> Dp = { it },
) {
    val imeVisible = WindowInsets.isImeVisible
    val imeHeight = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).dp }
    val height by animateDpAsState(if (imeVisible) confirmHeight(imeHeight) else 0.dp)
    Spacer(modifier.height(height))
}
