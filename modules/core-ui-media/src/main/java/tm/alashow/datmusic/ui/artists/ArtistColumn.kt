/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.click
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.ui.components.CoverImage
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
    analytics: FirebaseAnalytics = LocalAnalytics.current,
    onClick: (Artist) -> Unit = {},
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
                if (!isPlaceholder) onClick(artist)
            }
            .fillMaxWidth()
            .padding(AppTheme.specs.paddingTiny)
    ) {
        val image = rememberImagePainter(artist.photo(), builder = ImageLoading.defaultConfig)
        CoverImage(
            painter = image,
            icon = rememberVectorPainter(Icons.Default.Person),
            shape = CircleShape,
            size = imageSize
        ) { modifier ->
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.then(loadingModifier)
            )
        }

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
