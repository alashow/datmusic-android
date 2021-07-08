/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

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
import com.google.accompanist.coil.rememberCoilPainter
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.ui.components.CoverImage
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
    onClick: (Artist) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick(artist) }
            .fillMaxWidth()
            .padding(horizontal = AppTheme.specs.paddingTiny)
    ) {
        val image = rememberCoilPainter(artist.photo(), fadeIn = true)
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
                modifier = modifier
            )
        }
        Text(
            artist.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(nameWidth)
        )
    }
}
