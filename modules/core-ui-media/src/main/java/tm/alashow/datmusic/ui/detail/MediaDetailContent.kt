/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.compose.foundation.lazy.LazyListScope
import tm.alashow.domain.models.Async

abstract class MediaDetailContent<T> {

    abstract operator fun invoke(
        list: LazyListScope,
        details: Async<T>,
        detailsLoading: Boolean
    ): Boolean
}
