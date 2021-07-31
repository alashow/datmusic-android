/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.shimmer
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.click
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme

object AlbumsDefaults {
    val imageSize = 150.dp
    val iconPadding = 36.dp
}

@Composable
fun AlbumColumn(
    album: Album,
    modifier: Modifier = Modifier,
    imageSize: Dp = AlbumsDefaults.imageSize,
    iconPadding: Dp = AlbumsDefaults.iconPadding,
    isPlaceholder: Boolean = false,
    analytics: FirebaseAnalytics = LocalAnalytics.current,
    onClick: (Album) -> Unit = {},
) {
    val loadingModifier = Modifier.placeholder(
        visible = isPlaceholder,
        highlight = PlaceholderHighlight.shimmer(),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
        modifier = modifier
            .clickable {
                analytics.click("album", mapOf("id" to album.id))
                if (!isPlaceholder) onClick(album)
            }
            .fillMaxWidth()
            .padding(AppTheme.specs.padding)
    ) {
        val image = rememberImagePainter(album.photo.mediumUrl, builder = ImageLoading.defaultConfig)
        CoverImage(
            image, size = imageSize,
            icon = rememberVectorPainter(Icons.Default.Album),
            iconPadding = iconPadding
        ) { modifier ->
            Image(
                painter = image,
                contentDescription = null,
                modifier = modifier.then(loadingModifier)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.width(imageSize)
        ) {
            Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = loadingModifier, style = MaterialTheme.typography.body1)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    album.artists.firstOrNull()?.name ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = loadingModifier,
                    style = MaterialTheme.typography.body2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (album.explicit)
                        Icon(
                            painter = rememberVectorPainter(Icons.Filled.Explicit),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
                        )
                    Text(album.year.toString(), modifier = loadingModifier, style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}
