/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

/** Calculates the end padding with [LocalLayoutDirection]. **/
@Composable
internal fun PaddingValues.calculateEndPadding(): Dp {
    return calculateEndPadding(LocalLayoutDirection.current)
}

/** Calculates the start padding with [LocalLayoutDirection]. **/
@Composable
internal fun PaddingValues.calculateStartPadding(): Dp {
    return calculateStartPadding(LocalLayoutDirection.current)
}

/** Copies the given [PaddingValues]. **/
@Composable
fun PaddingValues.copy(
    start: Dp = calculateStartPadding(),
    top: Dp = calculateTopPadding(),
    end: Dp = calculateEndPadding(),
    bottom: Dp = calculateBottomPadding()
): PaddingValues {
    return PaddingValues(start, top, end, bottom)
}

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return copy(
        start = calculateStartPadding() + other.calculateStartPadding(),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding() + other.calculateEndPadding(),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}
