/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

private val CircularFont = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.circular_black,
            weight = FontWeight.Black,
            style = FontStyle.Normal
        ),
        Font(
            resId = R.font.circular_bold,
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        ),
        Font(
            resId = R.font.circular_regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        ),
        Font(
            resId = R.font.montserrat_light,
            weight = FontWeight.Light,
            style = FontStyle.Normal
        ),
    )
)

private val M3Typography = Typography()
val Typography = M3Typography.copy(
    displayLarge = M3Typography.displayLarge.copy(fontFamily = CircularFont),
    displayMedium = M3Typography.displayMedium.copy(fontFamily = CircularFont),
    displaySmall = M3Typography.displaySmall.copy(fontFamily = CircularFont),
    headlineLarge = M3Typography.headlineLarge.copy(fontFamily = CircularFont),
    headlineMedium = M3Typography.headlineMedium.copy(fontFamily = CircularFont),
    headlineSmall = M3Typography.headlineSmall.copy(fontFamily = CircularFont),
    titleLarge = M3Typography.titleLarge.copy(fontFamily = CircularFont),
    titleMedium = M3Typography.titleMedium.copy(fontFamily = CircularFont),
    titleSmall = M3Typography.titleSmall.copy(fontFamily = CircularFont),
    bodyLarge = M3Typography.bodyLarge.copy(fontFamily = CircularFont),
    bodyMedium = M3Typography.bodyMedium.copy(fontFamily = CircularFont),
    bodySmall = M3Typography.bodySmall.copy(fontFamily = CircularFont),
    labelLarge = M3Typography.labelLarge.copy(fontFamily = CircularFont),
    labelMedium = M3Typography.labelMedium.copy(fontFamily = CircularFont),
    labelSmall = M3Typography.labelSmall.copy(fontFamily = CircularFont),
)
