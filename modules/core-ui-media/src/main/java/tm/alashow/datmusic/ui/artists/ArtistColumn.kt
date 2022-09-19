/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.base.util.Analytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.placeholder
import tm.alashow.ui.components.shimmer
import tm.alashow.ui.theme.AppTheme

object ArtistsDefaults {
    val imageSize = 70.dp
    val nameWidth = 100.dp
}

@Composable
fun ArtistColumn(
    artist: Artist,
    imageSize: Dp = ArtistsDefaults.imageSize,
    nameWidth: Dp = ArtistsDefaults.nameWidth,
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                analytics.click("artist", mapOf("id" to artist.id))
                if (!isPlaceholder) onClick()
            }
            .padding(AppTheme.specs.paddingTiny)
    ) {
        CoverImage(
            data = artist.photo(),
            icon = rememberVectorPainter(Icons.Default.Person),
            shape = CircleShape,
            size = imageSize,
            imageModifier = loadingModifier,
        )

        Text(
            artist.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(nameWidth)
                .then(loadingModifier)
        )
    }
}

@CombinedPreview
@Composable
fun ArtistColumnPreview() = PreviewDatmusicCore {
    Surface {
        ArtistColumn(SampleData.artist())
    }
}
