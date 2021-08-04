/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import kotlinx.coroutines.launch
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.extensions.orNA
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.artworkUri
import tm.alashow.datmusic.playback.displayDescription
import tm.alashow.ui.adaptiveGradient
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.plainBackground

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaybackSheet(bottomSheetState: BottomSheetState = LocalPlaybackSheetState.current, content: @Composable (PaddingValues) -> Unit) {
    val coroutine = rememberCoroutineScope()
    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberUpdatedState(bottomSheetState).value
    )
    val collapse = {
        coroutine.launch {
            sheetScaffoldState.bottomSheetState.collapse()
        }
        Unit
    }

    if (sheetScaffoldState.bottomSheetState.isExpanded) {
        BackHandler(onBack = collapse)
    }

    BottomSheetScaffold(
        backgroundColor = Color.Transparent,
        scaffoldState = sheetScaffoldState,
        sheetContent = {
            PlaybackSheetContent(onClose = collapse)
        },
        sheetPeekHeight = 0.dp,
        content = content,
    )
}

@Composable
fun PlaybackSheetContent(
    onClose: () -> Unit,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)
    val adaptiveGradient = adaptiveGradient(nowPlaying.artwork)

    Scaffold(
        backgroundColor = Color.Transparent,
        modifier = Modifier.background(adaptiveGradient.second),
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = -36.dp)
                    ) {
                        Text(
                            nowPlaying.displayDescription.orNA()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                            modifier = Modifier.size(36.dp),
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {

            val imageSize = maxHeight / 2f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.specs.paddingLarge)
            ) {
                val artwork = rememberImagePainter(nowPlaying.artworkUri, builder = ImageLoading.defaultConfig)
                CoverImage(
                    painter = artwork,
                    size = imageSize,
                    shape = RectangleShape,
                    backgroundColor = MaterialTheme.colors.plainBackground(),
                    contentColor = adaptiveGradient.first.value,
                    bitmapPlaceholder = nowPlaying.artwork,
                ) { imageMod ->
                    Image(
                        painter = artwork,
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null,
                        modifier = imageMod,
                    )
                }
            }
        }
    }
}
