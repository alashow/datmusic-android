/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.ui.components.EmptyErrorBox

open class MediaDetailEmpty<T> {

    open operator fun invoke(
        list: LazyListScope,
        details: Async<T>,
        isHeaderVisible: Boolean,
        detailsEmpty: Boolean,
        onEmptyRetry: () -> Unit
    ) {
        if (details is Success && detailsEmpty) {
            list.item {
                EmptyErrorBox(
                    onRetryClick = onEmptyRetry,
                    modifier = Modifier.fillParentMaxHeight(if (isHeaderVisible) 0.5f else 1f)
                )
            }
        }
    }
}
