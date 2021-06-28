/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.EntityList
import tm.alashow.datmusic.ui.EntityListRow
import tm.alashow.datmusic.ui.theme.AppTheme

@Composable
internal fun AudioList(viewModel: SearchViewModel, padding: PaddingValues) {
    EntityList(
        lazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedAudioList).collectAsLazyPagingItems(),
        padding = padding
    ) {
        val audio = it ?: return@EntityList

        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppTheme.specs.padding)
        ) {
            val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
            Image(
                painter = image,
                contentDescription = null,
                Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .placeholder(
                        visible = image.loadState is ImageLoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Spacer(Modifier.width(AppTheme.specs.padding))
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
                Text(audio.title)
                Text(audio.artist)
            }
        }
    }
}

@Composable
internal fun ArtistList(viewModel: SearchViewModel, padding: PaddingValues) {
    EntityListRow(
        lazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedArtistsList).collectAsLazyPagingItems(),
        padding = padding
    ) {
        val audio = it ?: return@EntityListRow

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.specs.padding)
        ) {
            val image = rememberCoilPainter(audio.photo?.url, fadeIn = true)
            Image(
                painter = image,
                contentDescription = null,
                Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .placeholder(
                        visible = image.loadState is ImageLoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Text(audio.name)
        }
    }
}
