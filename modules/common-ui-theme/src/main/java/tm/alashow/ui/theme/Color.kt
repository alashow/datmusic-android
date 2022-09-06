/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.material.ContentAlpha
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
import kotlin.random.Random

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

internal val DarkAppColors = appDarkColors(Primary, Secondary, PrimaryVariant, SecondaryVariant)
internal val LightAppColors = appLightColors(Primary, Secondary, PrimaryVariant, SecondaryVariant)

fun appDarkColors(
    primary: Color,
    secondary: Color,
    background: Color = primary,
    surface: Color = primary,
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onSurface: Color = Color.White,
    onSurfaceInputBackground: Color = Color(0x45706d86),
) = AppColors(
    isLight = false,
    _onSurfaceInputBackground = onSurfaceInputBackground,
    _materialColors = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = secondary, // TODO: create a separate color
        onTertiary = onSecondary, // TODO: create a separate color
        background = background,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surface, // TODO: create a separate color
        onSurfaceVariant = onSurface, // TODO: create a separate color
    )
)

fun appLightColors(
    primary: Color,
    secondary: Color,
    background: Color = Color.White,
    surface: Color = Color.White,
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onSurface: Color = Color.Black,
    onSurfaceInputBackground: Color = Color(0x45c1bbc0),
) = AppColors(
    isLight = true,
    _onSurfaceInputBackground = onSurfaceInputBackground,
    _materialColors = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        tertiary = secondary, // TODO: create a separate color
        onTertiary = onSecondary, // TODO: create a separate color
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surface, // TODO: create a separate color
        onSurfaceVariant = onSurface, // TODO: create a separate color
    )
)

@Composable
fun plainSurfaceColor() = if (AppTheme.colors.isLight) Color.White else Color.Black

@Composable
fun plainBackgroundColor() = if (!AppTheme.colors.isLight) Color.White else Color.Black

@Composable
fun plainGrayBackground() = if (AppTheme.colors.isLight) Color.LightGray else Color.DarkGray

@Composable
fun Color.disabledAlpha(condition: Boolean): Color = copy(alpha = if (condition) alpha else ContentAlpha.disabled)

@Composable
fun Color.contrastComposite(alpha: Float = 0.1f) = contentColorFor(this).copy(alpha = alpha).compositeOver(this)

// @Composable
// internal fun animate(colors: ColorScheme): ColorScheme {
//    val animationSpec = remember { spring<Color>() }
//
//    @Composable
//    fun animateColor(color: Color): Color = animateColorAsState(targetValue = color, animationSpec = animationSpec).value
//
//    return ColorScheme(
//        primary = animateColor(colors.primary),
//        secondary = animateColor(colors.secondary),
//        background = animateColor(colors.background),
//        surface = animateColor(colors.surface),
//        error = animateColor(colors.error),
//        onPrimary = animateColor(colors.onPrimary),
//        onSecondary = animateColor(colors.onSecondary),
//        onBackground = animateColor(colors.onBackground),
//        onSurface = animateColor(colors.onSurface),
//        onError = animateColor(colors.onError),
//        // TODO: animate rest
//    )
// }

@Composable
fun translucentSurfaceColor() = MaterialTheme.colorScheme.surface.copy(alpha = AppBarAlphas.translucentBarAlpha())

fun Modifier.translucentSurface() = composed { background(translucentSurfaceColor()) }

@Composable
fun Modifier.randomBackground(memoize: Boolean = true) = background(if (memoize) remember { randomColor() } else randomColor())

fun randomColor() = Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))

fun Color.fallbackTo(color: Color): Color = if (isUnspecified) color else this
