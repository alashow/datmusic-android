/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose.previews

import androidx.compose.ui.tooling.preview.Preview

private const val Group = "Font Scales"

@Preview(
    name = "Small Font Scale",
    group = Group,
    fontScale = 0.5f,
)
@Preview(
    name = "Large Font Scale",
    group = Group,
    fontScale = 1.5f,
)
annotation class FontScalePreview
