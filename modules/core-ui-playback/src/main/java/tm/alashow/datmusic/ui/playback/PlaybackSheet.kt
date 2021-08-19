/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import kotlin.math.roundToLong
import kotlinx.coroutines.launch
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.base.util.extensions.Callback
import tm.alashow.base.util.extensions.orNA
import tm.alashow.base.util.extensions.toFloat
import tm.alashow.base.util.millisToDuration
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.CoverImageSize
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artist
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.hasNext
import tm.alashow.datmusic.playback.hasPrevious
import tm.alashow.datmusic.playback.isBuffering
import tm.alashow.datmusic.playback.isError
import tm.alashow.datmusic.playback.isPlayEnabled
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.models.PlaybackModeState
import tm.alashow.datmusic.playback.models.PlaybackProgressState
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.QueueTitle
import tm.alashow.datmusic.playback.models.toAlbumSearchQuery
import tm.alashow.datmusic.playback.models.toArtistSearchQuery
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.playback.title
import tm.alashow.datmusic.playback.toggleRepeatMode
import tm.alashow.datmusic.playback.toggleShuffleMode
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.AudioDropdownMenu
import tm.alashow.datmusic.ui.audios.AudioItemAction
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.currentPlayingMenuActionLabels
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.ADAPTIVE_COLOR_ANIMATION
import tm.alashow.ui.Delayed
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.coloredRippleClickable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.material.Slider
import tm.alashow.ui.material.SliderDefaults
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.LocalThemeState
import tm.alashow.ui.theme.disabledAlpha
import tm.alashow.ui.theme.plainBackground

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaybackSheet(
    // override local theme color palette because we want simple colors for menus n' stuff
    sheetTheme: ThemeState = LocalThemeState.current.copy(colorPalettePreference = ColorPalettePreference.Black),
    bottomSheetState: BottomSheetState = LocalPlaybackSheetState.current,
    sheetContentWrapper: @Composable (@Composable () -> Unit) -> Unit = {},
    content: @Composable () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()
    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberUpdatedState(bottomSheetState).value,
    )
    val isExpanded = sheetScaffoldState.bottomSheetState.isExpanded

    val scrollToTop: Callback = {
        coroutine.launch {
            listState.animateScrollToItem(0)
        }
    }
    val collapse: Callback = {
        coroutine.launch {
            sheetScaffoldState.bottomSheetState.collapse()
            scrollToTop()
        }
    }

    if (isExpanded) {
        BackHandler(onBack = collapse)
    }

    BottomSheetScaffold(
        sheetPeekHeight = 0.dp,
        backgroundColor = Color.Transparent,
        scaffoldState = sheetScaffoldState,
        sheetContent = {
            sheetContentWrapper {
                AppTheme(theme = sheetTheme, changeSystemBar = false) {
                    PlaybackSheetContent(
                        onClose = collapse,
                        scrollToTop = scrollToTop,
                        listState = listState
                    )
                }
            }
        },
        content = { content() },
    )
}

@Composable
fun PlaybackSheetContent(
    onClose: Callback,
    scrollToTop: Callback,
    listState: LazyListState,
    scaffoldState: ScaffoldState = rememberScaffoldState(snackbarHostState = LocalScaffoldState.current.snackbarHostState),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue).collectAsState(PlaybackQueue())
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)

    val adaptiveColor = adaptiveColor(nowPlaying.artwork)
    val contentColor by animateColorAsState(adaptiveColor.color, ADAPTIVE_COLOR_ANIMATION)

    Scaffold(
        backgroundColor = Color.Transparent,
        modifier = Modifier.background(adaptiveColor.gradient),
        scaffoldState = scaffoldState,
        snackbarHost = {
            DismissableSnackbarHost(it, modifier = Modifier.navigationBarsPadding())
        },
    ) { paddingValues ->
        BoxWithConstraints {
            LazyColumn(
                state = listState,
                contentPadding = paddingValues,
            ) {
                item {
                    PlaybackSheetTopBar(
                        playbackQueue = playbackQueue,
                        onClose = onClose
                    )
                    val topPadding = AppTheme.specs.padding
                    Spacer(Modifier.height(topPadding))
                }

                item {
                    PlaybackPager(
                        nowPlaying = nowPlaying,
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) { audio, _, pagerMod ->
                        val currentArtwork = rememberImagePainter(audio.coverUri(CoverImageSize.LARGE))
                        PlaybackArtwork(currentArtwork, contentColor, nowPlaying, pagerMod)
                    }
                }

                playbackNowPlayingWithControls(nowPlaying, playbackState, contentColor, onClose)

                playbackQueue(
                    playbackQueue = playbackQueue,
                    scrollToTop = scrollToTop,
                    playbackConnection = playbackConnection,
                )

                item { Spacer(Modifier.navigationBarsHeight()) }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun LazyListScope.playbackQueue(
    playbackQueue: PlaybackQueue,
    scrollToTop: Callback,
    playbackConnection: PlaybackConnection,
) {
    val lastIndex = playbackQueue.audiosList.size
    val firstIndex = (playbackQueue.currentIndex + 1).coerceAtMost(lastIndex)
    val queue = playbackQueue.audiosList.subList(firstIndex, lastIndex)
    itemsIndexed(queue, key = { index, _ -> index }) { index, audio ->
        val realPosition = firstIndex + index
        AudioRow(
            audio = audio,
            imageSize = 40.dp,
            onPlayAudio = {
                playbackConnection.transportControls?.skipToQueueItem(realPosition.toLong())
                scrollToTop()
            }
        )
    }
}

private fun LazyListScope.playbackNowPlayingWithControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    onClose: Callback,
) {
    item {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(AppTheme.specs.paddingLarge)
        ) {
            PlaybackNowPlaying(nowPlaying = nowPlaying, onClose = onClose)

            PlaybackProgress(
                playbackState = playbackState,
                contentColor = contentColor
            )

            PlaybackControls(
                playbackState = playbackState,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun PlaybackArtwork(
    currentArtwork: ImagePainter,
    contentColor: Color,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    CoverImage(
        painter = currentArtwork,
        shape = RectangleShape,
        backgroundColor = MaterialTheme.colors.plainBackground(),
        contentColor = contentColor,
        bitmapPlaceholder = nowPlaying.artwork,
        modifier = Modifier
            .padding(horizontal = AppTheme.specs.paddingLarge)
            .fillMaxWidth()
            .then(modifier)
    ) { imageMod ->
        Image(
            painter = currentArtwork,
            contentDescription = null,
            modifier = Modifier
                .coloredRippleClickable(
                    onClick = {
                        playbackConnection.mediaController?.playPause()
                    },
                    color = contentColor,
                    rippleRadius = Dp.Unspecified,
                )
                .then(imageMod),
        )
    }
}

@Composable
private fun PlaybackNowPlaying(
    nowPlaying: MediaMetadataCompat,
    onClose: Callback,
    navigator: Navigator = LocalNavigator.current
) {
    val title = nowPlaying.title
    Text(
        title.orNA(),
        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier.simpleClickable {
            navigator.navigate(LeafScreen.Search.buildRoute(nowPlaying.toAlbumSearchQuery(), DatmusicSearchParams.BackendType.ALBUMS))
            onClose()
        }
    )
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            nowPlaying.artist.orNA(),
            style = MaterialTheme.typography.subtitle1,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.simpleClickable {
                navigator.navigate(
                    LeafScreen.Search.buildRoute(
                        nowPlaying.toArtistSearchQuery(),
                        DatmusicSearchParams.BackendType.ARTISTS,
                        DatmusicSearchParams.BackendType.ALBUMS
                    )
                )
                onClose()
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    thumbRadius: Dp = 4.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress).collectAsState(PlaybackProgressState())

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    Box {
        PlaybackProgressSlider(playbackState, progressState, draggingProgress, setDraggingProgress, thumbRadius, contentColor)
        PlaybackProgressDuration(progressState, draggingProgress, thumbRadius)
    }
}

@Composable
private fun PlaybackProgressSlider(
    playbackState: PlaybackStateCompat,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    thumbRadius: Dp,
    contentColor: Color,
    bufferedProgressColor: Color = contentColor.copy(alpha = 0.25f),
    height: Dp = 44.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val sliderColors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = ContentAlpha.disabled)
    )
    val linearProgressMod = Modifier
        .fillMaxWidth(fraction = .99f) // reduce linearProgressIndicators width to match Slider's
        .clip(CircleShape) // because Slider is rounded

    val bufferedProgress = progressState.bufferedProgress
    val isBuffering = playbackState.isBuffering

    Box(
        modifier = Modifier.height(height),
        contentAlignment = Alignment.Center
    ) {
        if (!isBuffering)
            LinearProgressIndicator(
                progress = bufferedProgress,
                color = bufferedProgressColor,
                backgroundColor = Color.Transparent,
                modifier = linearProgressMod
            )

        Slider(
            value = draggingProgress ?: progressState.progress,
            onValueChange = {
                if (!isBuffering) setDraggingProgress(it)
            },
            thumbRadius = thumbRadius,
            colors = sliderColors,
            modifier = Modifier.alpha(isBuffering.not().toFloat()),
            onValueChangeFinished = {
                playbackConnection.transportControls?.seekTo(
                    (updatedProgressState.total.toFloat() * (updatedDraggingProgress ?: 0f)).roundToLong()
                )
                setDraggingProgress(null)
            }
        )

        if (isBuffering) {
            LinearProgressIndicator(
                progress = 0f,
                color = contentColor,
                modifier = linearProgressMod
            )
            Delayed(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(linearProgressMod)
            ) {
                LinearProgressIndicator(
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PlaybackProgressDuration(
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    thumbRadius: Dp
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = thumbRadius)
            .align(Alignment.BottomCenter)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            Text(currentDuration, style = MaterialTheme.typography.caption)
            Text(progressState.totalDuration, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun PlaybackControls(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    modifier: Modifier = Modifier,
    smallRippleRadius: Dp = 30.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val playbackMode by rememberFlowWithLifecycle(playbackConnection.playbackMode).collectAsState(PlaybackModeState())
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { playbackConnection.mediaController?.toggleShuffleMode() },
            modifier = Modifier.size(20.dp),
            rippleRadius = smallRippleRadius,
        ) {
            Icon(
                painter = rememberVectorPainter(
                    when (playbackMode.shuffleMode) {
                        PlaybackStateCompat.SHUFFLE_MODE_NONE -> Icons.Default.Shuffle
                        PlaybackStateCompat.SHUFFLE_MODE_ALL -> Icons.Default.ShuffleOn
                        else -> Icons.Default.Shuffle
                    }
                ),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(AppTheme.specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.transportControls?.skipToPrevious() },
            modifier = Modifier.size(40.dp),
            rippleRadius = smallRippleRadius,
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.SkipPrevious),
                tint = contentColor.disabledAlpha(playbackState.hasPrevious),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(AppTheme.specs.padding))

        IconButton(
            onClick = { playbackConnection.mediaController?.playPause() },
            modifier = Modifier.size(80.dp),
            rippleRadius = 35.dp,
        ) {
            Icon(
                painter = rememberVectorPainter(
                    when {
                        playbackState.isError -> Icons.Filled.ErrorOutline
                        playbackState.isPlaying -> Icons.Filled.PauseCircleFilled
                        playbackState.isPlayEnabled -> Icons.Filled.PlayCircleFilled
                        else -> Icons.Filled.PlayCircleFilled
                    }
                ),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(AppTheme.specs.padding))

        IconButton(
            onClick = { playbackConnection.transportControls?.skipToNext() },
            modifier = Modifier.size(40.dp),
            rippleRadius = smallRippleRadius,
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.SkipNext),
                tint = contentColor.disabledAlpha(playbackState.hasNext),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(AppTheme.specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.mediaController?.toggleRepeatMode() },
            modifier = Modifier.size(20.dp),
            rippleRadius = smallRippleRadius,
        ) {
            Icon(
                painter = rememberVectorPainter(
                    when (playbackMode.repeatMode) {
                        PlaybackStateCompat.REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
                        PlaybackStateCompat.REPEAT_MODE_ALL -> Icons.Default.RepeatOn
                        else -> Icons.Default.Repeat
                    }
                ),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun PlaybackSheetTopBar(
    playbackQueue: PlaybackQueue,
    onClose: Callback,
    iconSize: Dp = 36.dp,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    TopAppBar(
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = -8.dp) // idk why this is needed for centering
            ) {
                val context = LocalContext.current
                val queueTitle = QueueTitle.from(playbackQueue.title ?: "")
                Text(
                    queueTitle.localizeType(context).uppercase(),
                    style = MaterialTheme.typography.overline.copy(fontWeight = FontWeight.Light),
                    maxLines = 1,
                )
                Text(
                    queueTitle.localizeValue(context), style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                    modifier = Modifier.size(iconSize),
                    contentDescription = null,
                )
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                AudioDropdownMenu(
                    expanded = expanded,
                    onExpandedChange = setExpanded,
                    actionLabels = currentPlayingMenuActionLabels,
                ) {
                    if (playbackQueue.isValid)
                        actionHandler(AudioItemAction.from(it, playbackQueue.currentAudio))
                }
            }
        }
    )
}
