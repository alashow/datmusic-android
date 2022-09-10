/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import tm.alashow.common.compose.copy
import tm.alashow.common.compose.plus

/**
 * Sum of all previous Scaffold [PaddingValues] in the hierarchy.
 */
val LocalScaffoldPadding: ProvidableCompositionLocal<PaddingValues> = staticCompositionLocalOf { PaddingValues(0.dp) }

@Composable
fun ProvideScaffoldPadding(padding: PaddingValues, content: @Composable () -> Unit) {
    val absolutePadding = (LocalScaffoldPadding.current + padding)
        .copy(top = padding.calculateTopPadding()) // reset top padding to avoid double padding because for some goddamn reason M3 Scaffold only adds up top padding
    CompositionLocalProvider(
        LocalScaffoldPadding provides absolutePadding,
        content = content
    )
}

@Composable
fun scaffoldPadding(): PaddingValues = LocalScaffoldPadding.current
