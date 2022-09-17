/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.SecureRandom
import kotlin.math.ln
import kotlin.random.Random
import tm.alashow.ui.material.ContentAlpha

fun parseColor(hexColor: String) = Color(AndroidColor.parseColor(hexColor))
fun Int.toColor() = Color(this)

val Primary = Color(0xFF16053D)
val PrimaryVariant = Color(0xFF221652)
val Secondary = Color(0xFFFF3C8F)
val SecondaryVariant = Color(0xFFef0076)

val WhiteTransparent = Color(0x80FFFFFF)

val Red = Color(0xFFFF3B30)
val Red700 = Color(0xFFC0392b)
val Orange = Color(0xFFFF9500)
val Yellow = Color(0xFFFFCC00)
val Yellow500 = Color(0xFFFBBC04)
val Green = Color(0xFF4CD964)
val Blue300 = Color(0xFF5AC8FA)
val Blue = Color(0xFF007AFF)
val Purple = Color(0xFF5856D6)
val Asphalt = Color(0xFF2c3e50)

val Gray1000 = Color(0xFF121212)
val BlueGrey = Color(0xFF263238)
val Green600 = Color(0xFF1DB954)
val Green900 = Color(0xFF468847)

internal val DarkAppColors = appDarkColors(
    primary = Primary,
    secondary = Secondary,
    surfaceVariant = PrimaryVariant,
)
internal val LightAppColors = appLightColors(
    primary = Primary,
    secondary = Secondary,
    surfaceVariant = PrimaryVariant,
)

fun appDarkColors(
    primary: Color,
    secondary: Color,
    tertiary: Color = secondary,
    background: Color = primary,
    surface: Color = primary,
    surfaceTint: Color = secondary,
    surfaceVariant: Color = surface,
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onTertiary: Color = onSecondary,
    onSurface: Color = Color.White,
    onSurfaceVariant: Color = onSurface,
) = AppColors(
    _isLightTheme = false,
    _colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
    )
)

fun appLightColors(
    primary: Color,
    secondary: Color,
    tertiary: Color = secondary,
    background: Color = Color.White,
    surface: Color = Color.White,
    surfaceTint: Color = secondary,
    surfaceVariant: Color = surface,
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onTertiary: Color = onSecondary,
    onSurface: Color = Color.Black,
    onSurfaceVariant: Color = onSurface,
) = AppColors(
    _isLightTheme = true,
    _colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        tertiary = tertiary,
        onTertiary = onTertiary,
        surfaceTint = surfaceTint,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
    )
)

@Composable
fun plainSurfaceColor() = if (AppTheme.colors.isLightTheme) Color.White else Color.Black

@Composable
fun plainBackgroundColor() = if (!AppTheme.colors.isLightTheme) Color.White else Color.Black

@Composable
fun plainGrayBackground() = if (AppTheme.colors.isLightTheme) Color.LightGray else Color.DarkGray

@Composable
fun Color.disabledAlpha(condition: Boolean): Color = copy(alpha = if (condition) alpha else ContentAlpha.disabled)

@Composable
fun Color.contrastComposite(alpha: Float = 0.1f) = contentColorFor(this).copy(alpha = alpha).compositeOver(this)

fun Color.colorAtElevation(tint: Color, elevation: Dp): Color {
    if (elevation == 0.dp) return this
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return tint.copy(alpha = alpha).compositeOver(this)
}

@Composable
fun translucentSurfaceColor() = MaterialTheme.colorScheme.surface.copy(alpha = AppBarAlphas.translucentBarAlpha())

fun Modifier.translucentSurface() = composed { background(translucentSurfaceColor()) }

@Composable
fun Modifier.randomBackground(memoize: Boolean = true) = background(if (memoize) remember { randomColor() } else randomColor())

private val Randomness = Random(SecureRandom().nextLong())
fun randomColor() = Color(Randomness.nextInt(255), Randomness.nextInt(255), Randomness.nextInt(255), Randomness.nextInt(255))

fun Color.fallbackTo(color: Color): Color = if (isUnspecified) color else this

/**
 * Animates [colorScheme] colors when it changes.
 *
 * @see [Theme.un]
 */
@Composable
internal fun animate(
    colorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
): ColorScheme {

    @Composable
    fun animateColor(color: Color): Color = animateColorAsState(targetValue = color, animationSpec = animationSpec).value

    return ColorScheme(
        primary = animateColor(colorScheme.primary),
        onPrimary = animateColor(colorScheme.onPrimary),
        primaryContainer = animateColor(colorScheme.primaryContainer),
        onPrimaryContainer = animateColor(colorScheme.onPrimaryContainer),
        inversePrimary = animateColor(colorScheme.inversePrimary),
        secondary = animateColor(colorScheme.secondary),
        onSecondary = animateColor(colorScheme.onSecondary),
        secondaryContainer = animateColor(colorScheme.secondaryContainer),
        onSecondaryContainer = animateColor(colorScheme.onSecondaryContainer),
        tertiary = animateColor(colorScheme.tertiary),
        onTertiary = animateColor(colorScheme.onTertiary),
        tertiaryContainer = animateColor(colorScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(colorScheme.onTertiaryContainer),
        background = animateColor(colorScheme.background),
        onBackground = animateColor(colorScheme.onBackground),
        surface = animateColor(colorScheme.surface),
        onSurface = animateColor(colorScheme.onSurface),
        surfaceVariant = animateColor(colorScheme.surfaceVariant),
        onSurfaceVariant = animateColor(colorScheme.onSurfaceVariant),
        surfaceTint = animateColor(colorScheme.surfaceTint),
        inverseSurface = animateColor(colorScheme.inverseSurface),
        inverseOnSurface = animateColor(colorScheme.inverseOnSurface),
        error = animateColor(colorScheme.error),
        onError = animateColor(colorScheme.onError),
        errorContainer = animateColor(colorScheme.errorContainer),
        onErrorContainer = animateColor(colorScheme.onErrorContainer),
        outline = animateColor(colorScheme.outline),
        outlineVariant = animateColor(colorScheme.outlineVariant),
        scrim = animateColor(colorScheme.scrim),
    )
}
