/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.material

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.luminance
import tm.alashow.ui.theme.Theme

// Cloned from material2 since it was removed in material3

/**
 * Default alpha levels used by Material components.
 *
 * See [LocalContentAlpha].
 */
object ContentAlpha {
    /**
     * A high level of content alpha, used to represent high emphasis text such as input text in a
     * selected [TextField].
     */
    val high: Float
        @Composable
        get() = contentAlpha(
            highContrastAlpha = HighContrastContentAlpha.high,
            lowContrastAlpha = LowContrastContentAlpha.high
        )

    /**
     * A medium level of content alpha, used to represent medium emphasis text such as
     * placeholder text in a [TextField].
     */
    val medium: Float
        @Composable
        get() = contentAlpha(
            highContrastAlpha = HighContrastContentAlpha.medium,
            lowContrastAlpha = LowContrastContentAlpha.medium
        )

    /**
     * A low level of content alpha used to represent disabled components, such as text in a
     * disabled [Button].
     */
    val disabled: Float
        @Composable
        get() = contentAlpha(
            highContrastAlpha = HighContrastContentAlpha.disabled,
            lowContrastAlpha = LowContrastContentAlpha.disabled
        )

    /**
     * This default implementation uses separate alpha levels depending on the luminance of the
     * incoming color, and whether the theme is light or dark. This is to ensure correct contrast
     * and accessibility on all surfaces.
     *
     * See [HighContrastContentAlpha] and [LowContrastContentAlpha] for what the levels are
     * used for, and under what circumstances.
     */
    @Composable
    private fun contentAlpha(
        /*@FloatRange(from = 0.0, to = 1.0)*/
        highContrastAlpha: Float,
        /*@FloatRange(from = 0.0, to = 1.0)*/
        lowContrastAlpha: Float
    ): Float {
        val contentColor = LocalContentColor.current
        val lightTheme = Theme.isLight
        return if (lightTheme) {
            if (contentColor.luminance() > 0.5) highContrastAlpha else lowContrastAlpha
        } else {
            if (contentColor.luminance() < 0.5) highContrastAlpha else lowContrastAlpha
        }
    }
}

/**
 * CompositionLocal containing the preferred content alpha for a given position in the hierarchy.
 * This alpha is used for text and iconography ([Text] and [Icon]) to emphasize / de-emphasize
 * different parts of a component. See the Material guide on
 * [Text Legibility](https://material.io/design/color/text-legibility.html) for more information on
 * alpha levels used by text and iconography.
 *
 * See [ContentAlpha] for the default levels used by most Material components.
 *
 * [MaterialTheme] sets this to [ContentAlpha.high] by default, as this is the default alpha for
 * body text.
 *
 * @sample androidx.compose.material.samples.ContentAlphaSample
 */
val LocalContentAlpha = compositionLocalOf { 1f }

/**
 * Alpha levels for high luminance content in light theme, or low luminance content in dark theme.
 *
 * This content will typically be placed on colored surfaces, so it is important that the
 * contrast here is higher to meet accessibility standards, and increase legibility.
 *
 * These levels are typically used for text / iconography in primary colored tabs /
 * bottom navigation / etc.
 */
private object HighContrastContentAlpha {
    const val high: Float = 1.00f
    const val medium: Float = 0.74f
    const val disabled: Float = 0.38f
}

/**
 * Alpha levels for low luminance content in light theme, or high luminance content in dark theme.
 *
 * This content will typically be placed on grayscale surfaces, so the contrast here can be lower
 * without sacrificing accessibility and legibility.
 *
 * These levels are typically used for body text on the main surface (white in light theme, grey
 * in dark theme) and text / iconography in surface colored tabs / bottom navigation / etc.
 */
private object LowContrastContentAlpha {
    const val high: Float = 0.87f
    const val medium: Float = 0.60f
    const val disabled: Float = 0.38f
}

/**
 * Provides [LocalContentAlpha] and overrides [LocalContentColor] alpha with [contentAlpha].
 * This is because M3 doesn't support providing [ContentAlpha] for components anymore.
 */
@Composable
fun ProvideContentAlpha(
    contentAlpha: Float,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(alpha = contentAlpha),
        LocalContentAlpha provides contentAlpha,
        content = content
    )
}
