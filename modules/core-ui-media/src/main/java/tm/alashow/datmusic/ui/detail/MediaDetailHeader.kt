/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.statusBarsPadding
import tm.alashow.base.util.extensions.Callback
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme

class MediaDetailHeader {
    operator fun invoke(
        list: LazyListScope,
        headerBackgroundMod: Modifier,
        title: String,
        artwork: Any?,
        headerOffsetProgress: State<Float>,
        onTitleClick: Callback,
        extraHeaderContent: @Composable (ColumnScope.() -> Unit)
    ) {
        list.item {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = headerBackgroundMod
                    .padding(AppTheme.specs.padding)
                    .statusBarsPadding(),
            ) {
                CoverHeaderRow(
                    title = title,
                    imageData = artwork,
                    offsetProgress = headerOffsetProgress,
                    titleModifier = Modifier.simpleClickable(onClick = onTitleClick)
                )
                extraHeaderContent()
            }
        }
    }
}
