/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.material3.Typography as M3Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

private val M3TypographyDefault = M3Typography()
val M3Typography = M3TypographyDefault.copy(
    displayLarge = M3TypographyDefault.displayLarge.copy(fontFamily = CircularFont),
    displayMedium = M3TypographyDefault.displayMedium.copy(fontFamily = CircularFont),
    displaySmall = M3TypographyDefault.displaySmall.copy(fontFamily = CircularFont),
    headlineLarge = M3TypographyDefault.headlineLarge.copy(fontFamily = CircularFont),
    headlineMedium = M3TypographyDefault.headlineMedium.copy(fontFamily = CircularFont),
    headlineSmall = M3TypographyDefault.headlineSmall.copy(fontFamily = CircularFont),
    titleLarge = M3TypographyDefault.titleLarge.copy(fontFamily = CircularFont),
    titleMedium = M3TypographyDefault.titleMedium.copy(fontFamily = CircularFont),
    titleSmall = M3TypographyDefault.titleSmall.copy(fontFamily = CircularFont),
    bodyLarge = M3TypographyDefault.bodyLarge.copy(fontFamily = CircularFont),
    bodyMedium = M3TypographyDefault.bodyMedium.copy(fontFamily = CircularFont),
    bodySmall = M3TypographyDefault.bodySmall.copy(fontFamily = CircularFont),
    labelLarge = M3TypographyDefault.labelLarge.copy(fontFamily = CircularFont),
    labelMedium = M3TypographyDefault.labelMedium.copy(fontFamily = CircularFont),
    labelSmall = M3TypographyDefault.labelSmall.copy(fontFamily = CircularFont),
)

data class Typography(
    val h6: TextStyle = M3Typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
)

val DefaultTypography = Typography()
