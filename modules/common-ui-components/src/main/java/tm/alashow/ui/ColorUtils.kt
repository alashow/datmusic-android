/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import android.graphics.Bitmap
import android.graphics.Color as AColor
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import androidx.palette.graphics.Palette
import tm.alashow.ui.theme.contrastComposite
import tm.alashow.ui.theme.toColor

data class AdaptiveColorResult(val color: Color, val contentColor: Color, val gradient: Brush)

@Composable
fun adaptiveColor(
    image: Bitmap? = null,
    initial: Color = MaterialTheme.colors.onSurface.contrastComposite(),
    fallback: Color = MaterialTheme.colors.secondary,
): AdaptiveColorResult {
    var accent by remember { mutableStateOf(initial) }
    val contentColor by derivedStateOf { accent.contentColor() }

    val isDarkColors = !MaterialTheme.colors.isLight

    LaunchedEffect(image, fallback, isDarkColors) {
        if (image != null)
            Palette.from(image)
                .generate().apply {
                    accent = getAccentColor(isDarkColors, fallback.toArgb(), this).toColor()
                }
    }

    return AdaptiveColorResult(accent, contentColor, backgroundGradient(accent))
}

@Composable
fun backgroundGradient(
    accent: Color,
    endColor: Color = if (MaterialTheme.colors.isLight) Color.White else Color.Black
): Brush {
    val isDark = !MaterialTheme.colors.isLight
    val first = gradientShift(isDark, accent.toArgb(), 0.4f, 100)
    val second = gradientShift(isDark, accent.toArgb(), 0.13f, 25)

    return Brush.verticalGradient(listOf(first, second, endColor))
}

fun getAccentColor(isDark: Boolean, default: Int, palette: Palette): Int {
    when (isDark) {
        true -> {
            val darkMutedColor = palette.getDarkMutedColor(default)
            val lightMutedColor = palette.getLightMutedColor(darkMutedColor)
            val darkVibrant = palette.getDarkVibrantColor(lightMutedColor)
            val lightVibrant = palette.getLightVibrantColor(darkVibrant)
            val mutedColor = palette.getMutedColor(lightVibrant)
            return palette.getVibrantColor(mutedColor)
        }
        false -> {
            val lightMutedColor = palette.getLightMutedColor(default)
            val lightVibrant = palette.getLightVibrantColor(lightMutedColor)
            val mutedColor = palette.getMutedColor(lightVibrant)
            val darkMutedColor = palette.getDarkMutedColor(mutedColor)
            val vibrant = palette.getVibrantColor(darkMutedColor)
            return palette.getDarkVibrantColor(vibrant)
        }
    }
}

private fun gradientShift(isDarkMode: Boolean, color: Int, shift: Float, alpha: Int): Color {
    return Color(if (isDarkMode) shiftColor(color, shift) else ColorUtils.setAlphaComponent(shiftColor(color, 2f), alpha))
}

fun Color.contentColor() = getContrastColor(toArgb()).toColor()

fun getContrastColor(@ColorInt color: Int): Int {
    // Counting the perceptive luminance - human eye favors green color...
    val a: Double = 1 - (0.299 * AColor.red(color) + 0.587 * AColor.green(color) + 0.114 * AColor.blue(color)) / 255
    return if (a < 0.5) AColor.BLACK else AColor.WHITE
}

private fun desaturate(isDarkMode: Boolean, color: Int): Int {
    if (!isDarkMode) {
        return color
    }

    if (color == AColor.TRANSPARENT) {
        // can't desaturate transparent color
        return color
    }
    val amount = .25f
    val minDesaturation = .75f

    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.colorToHSL(color, hsl)
    if (hsl[1] > minDesaturation) {
        hsl[1] = MathUtils.clamp(
            hsl[1] - amount,
            minDesaturation - 0.1f,
            1f
        )
    }
    return ColorUtils.HSLToColor(hsl)
}

fun shiftColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 2.0) by: Float): Int {
    return if (by == 1.0f) {
        color
    } else {
        val alpha = AColor.alpha(color)
        val hsv = FloatArray(3)
        AColor.colorToHSV(color, hsv)
        hsv[2] *= by
        (alpha shl 24) + (16777215 and AColor.HSVToColor(hsv))
    }
}
