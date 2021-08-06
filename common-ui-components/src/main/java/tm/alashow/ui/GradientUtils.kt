/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import android.graphics.Bitmap
import android.graphics.Color as AColor
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import tm.alashow.ui.theme.toColor

data class AdaptiveGradientResult(val color: State<Color>, val gradient: Brush)

@Composable
fun adaptiveGradient(image: Bitmap? = null, initial: Color = MaterialTheme.colors.secondary): AdaptiveGradientResult {
    var accent by remember { mutableStateOf(initial) }
    val accentAnimated = animateColorAsState(accent)
    val isDarkTheme = !MaterialTheme.colors.isLight
    LaunchedEffect(image) {
        if (image != null)
            Palette.from(image)
                .generate().apply {
                    accent = getAccentColor(initial.toArgb(), this).toColor()
                }
    }

    return AdaptiveGradientResult(accentAnimated, backgroundGradient(accentAnimated.value))
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

fun getAccentColor(accent: Int, palette: Palette): Int {
    val mutedColor = palette.getMutedColor(accent)
    val lightVibrant = palette.getLightVibrantColor(mutedColor)
    return palette.getVibrantColor(lightVibrant)
}

private fun gradientShift(isDarkMode: Boolean, color: Int, shift: Float, alpha: Int): Color {
    return Color(if (isDarkMode) shiftColor(color, shift) else ColorUtils.setAlphaComponent(shiftColor(color, 2f), alpha))
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
