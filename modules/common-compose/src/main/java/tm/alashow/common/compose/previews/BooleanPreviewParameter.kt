/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanPreviewParameter : PreviewParameterProvider<Boolean> {

    override val values: Sequence<Boolean>
        get() = sequenceOf(false, true)
}
