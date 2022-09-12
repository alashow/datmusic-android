/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import javax.annotation.concurrent.Immutable

val ContentPaddingLarge = 28.dp
val ContentPadding = 16.dp
val ContentPaddingSmall = 8.dp
val ContentPaddingTiny = 4.dp

@Immutable
data class Specs(
    val padding: Dp = ContentPadding,
    val paddingSmall: Dp = ContentPaddingSmall,
    val paddingTiny: Dp = ContentPaddingTiny,
    val paddingLarge: Dp = ContentPaddingLarge,

    val paddings: PaddingValues = PaddingValues(padding),
    val inputPaddings: PaddingValues = PaddingValues(horizontal = padding, vertical = paddingSmall),

    val iconSize: Dp = 36.dp,
    val iconSizeSmall: Dp = 28.dp,
    val iconSizeTiny: Dp = 18.dp,
    val iconSizeLarge: Dp = 48.dp,
)

val DefaultSpecs = Specs()
