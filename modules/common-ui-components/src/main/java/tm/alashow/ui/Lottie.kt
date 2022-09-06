/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun colorFilterDynamicProperty(color: Color = MaterialTheme.colorScheme.secondary) = rememberLottieDynamicProperties(
    rememberLottieDynamicProperty(
        property = LottieProperty.COLOR_FILTER,
        value = SimpleColorFilter(color.toArgb()),
        keyPath = arrayOf(
            "**",
        )
    ),
)
