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

val LocalScaffoldPadding: ProvidableCompositionLocal<PaddingValues> = staticCompositionLocalOf { PaddingValues(0.dp) }

@Composable
fun ProvideScaffoldPadding(padding: PaddingValues, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalScaffoldPadding provides padding, content = content)
}

@Composable
fun scaffoldPadding(): PaddingValues = LocalScaffoldPadding.current
