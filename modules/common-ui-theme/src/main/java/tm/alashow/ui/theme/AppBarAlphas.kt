/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.runtime.Composable

object AppBarAlphas {
    @Composable
    fun translucentBarAlpha(): Float = when {
        AppTheme.colors.isLightTheme -> 0.97f
        else -> 0.95f
    }
}
