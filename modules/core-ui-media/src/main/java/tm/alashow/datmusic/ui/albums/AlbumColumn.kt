/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.base.util.Analytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.placeholder
import tm.alashow.ui.components.shimmer
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.AppTheme

object AlbumsDefaults {
    val imageSize = 150.dp
}

@Composable
fun AlbumColumn(
    album: Album,
    modifier: Modifier = Modifier,
    imageSize: Dp = AlbumsDefaults.imageSize,
    isPlaceholder: Boolean = false,
    analytics: Analytics = LocalAnalytics.current,
    onClick: () -> Unit = {},
) {
    val loadingModifier = Modifier.placeholder(
        visible = isPlaceholder,
        highlight = shimmer(),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
        modifier = modifier
            .clickable {
                analytics.click("album", mapOf("id" to album.id))
                if (!isPlaceholder) onClick()
            }
            .padding(AppTheme.specs.padding)
    ) {
        CoverImage(
            data = album.photo.mediumUrl,
            size = imageSize,
            icon = rememberVectorPainter(Icons.Default.Album),
            imageModifier = loadingModifier,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.width(imageSize)
        ) {
            Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = loadingModifier, style = MaterialTheme.typography.bodyLarge)
            ProvideContentAlpha(ContentAlpha.medium) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!album.hasYear && album.explicit)
                        ExplicitIcon()
                    Text(
                        album.artists.firstOrNull()?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = loadingModifier,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (album.hasYear)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (album.explicit)
                            ExplicitIcon()
                        Text(album.year.toString(), modifier = loadingModifier, style = MaterialTheme.typography.bodyMedium)
                    }
            }
        }
    }
}

@Composable
private fun ExplicitIcon() {
    Icon(
        painter = rememberVectorPainter(Icons.Filled.Explicit),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium),
    )
}

@CombinedPreview
@Composable
fun AlbumColumnPreview() = PreviewDatmusicCore {
    Surface {
        AlbumColumn(SampleData.album())
    }
}
