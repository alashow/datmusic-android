/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.downloader.audioHeader
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.isIdle
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.QueueTitle.Companion.asQueueTitle
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.AudioDropdownMenu
import tm.alashow.datmusic.ui.audios.AudioItemAction
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler
import tm.alashow.datmusic.ui.audios.currentPlayingMenuActionLabels
import tm.alashow.datmusic.ui.library.playlist.addTo.AddToPlaylistMenu
import tm.alashow.datmusic.ui.playback.components.PlaybackArtworkPagerWithNowPlayingAndControls
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.ADAPTIVE_COLOR_ANIMATION
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.ResizableLayout
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.MoreVerticalIcon
import tm.alashow.ui.isWideLayout
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.LocalThemeState
import tm.alashow.ui.theme.plainBackgroundColor

private val RemoveFromPlaylist = R.string.playback_queue_removeFromQueue
private val AddQueueToPlaylist = R.string.playback_queue_addQueueToPlaylist
private val SaveQueueAsPlaylist = R.string.playback_queue_saveAsPlaylist

@Composable
fun PlaybackSheet(
    // override local theme color palette because we want simple colors for menus n' stuff
    sheetTheme: ThemeState = LocalThemeState.current.copy(colorPalettePreference = ColorPalettePreference.Black),
    navigator: Navigator = LocalNavigator.current,
) {
    val listState = rememberLazyListState()
    val queueListState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()

    val scrollToTop: Callback = {
        coroutine.launch {
            listState.animateScrollToItem(0)
        }
    }

    val audioActionHandler = audioActionHandler()
    CompositionLocalProvider(LocalAudioActionHandler provides audioActionHandler) {
        AppTheme(theme = sheetTheme, changeSystemBar = false) {
            PlaybackSheetContent(
                onClose = { navigator.goBack() },
                scrollToTop = scrollToTop,
                listState = listState,
                queueListState = queueListState,
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun PlaybackSheetContent(
    onClose: Callback,
    scrollToTop: Callback,
    listState: LazyListState,
    queueListState: LazyListState,
    scaffoldState: ScaffoldState = rememberScaffoldState(snackbarHostState = LocalScaffoldState.current.snackbarHostState),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    viewModel: PlaybackViewModel = hiltViewModel(),
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
    val pagerState = rememberPagerState(playbackQueue.currentIndex)

    val adaptiveColor by adaptiveColor(nowPlaying.artwork, initial = MaterialTheme.colors.onBackground)
    val contentColor by animateColorAsState(adaptiveColor.color, ADAPTIVE_COLOR_ANIMATION)

    LaunchedEffect(playbackConnection) {
        playbackConnection.playbackState
            .filter { it != NONE_PLAYBACK_STATE }
            .collectLatest { if (it.isIdle) onClose() }
    }

    val contentPadding = rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyTop = true,
        applyBottom = true,
    )

    if (playbackState == NONE_PLAYBACK_STATE) {
        Row(Modifier.fillMaxSize()) { FullScreenLoading(delayMillis = 0) }
        return
    }

    BoxWithConstraints {
        val isWideLayout = isWideLayout()
        val maxWidth = maxWidth
        Row(Modifier.fillMaxSize()) {
            if (isWideLayout) {
                ResizablePlaybackQueue(
                    maxWidth = maxWidth,
                    playbackQueue = playbackQueue,
                    queueListState = queueListState,
                    scrollToTop = scrollToTop
                )
            }

            Scaffold(
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .background(adaptiveColor.gradient)
                    .weight(1f),
                scaffoldState = scaffoldState,
                snackbarHost = { DismissableSnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = contentPadding,
                ) {
                    item {
                        PlaybackSheetTopBar(
                            playbackQueue = playbackQueue,
                            onClose = onClose,
                            onTitleClick = viewModel::navigateToQueueSource,
                            onSaveQueueAsPlaylist = viewModel::saveQueueAsPlaylist
                        )
                        Spacer(Modifier.height(AppTheme.specs.paddingTiny))
                    }

                    item {
                        PlaybackArtworkPagerWithNowPlayingAndControls(
                            nowPlaying = nowPlaying,
                            playbackState = playbackState,
                            pagerState = pagerState,
                            contentColor = contentColor,
                            viewModel = viewModel,
                            modifier = Modifier.fillParentMaxHeight(0.8f),
                        )
                    }

                    if (playbackQueue.isValid)
                        item {
                            PlaybackAudioInfo(playbackQueue.currentAudio)
                        }

                    if (!isWideLayout && !playbackQueue.isLastAudio) {
                        playbackQueueLabel()
                        playbackQueue(
                            playbackQueue = playbackQueue,
                            scrollToTop = scrollToTop,
                            playbackConnection = playbackConnection,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ResizablePlaybackQueue(
    maxWidth: Dp,
    playbackQueue: PlaybackQueue,
    scrollToTop: Callback,
    queueListState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    resizableLayoutViewModel: ResizablePlaybackSheetLayoutViewModel = hiltViewModel(),
    dragOffset: State<Float> = rememberFlowWithLifecycle(resizableLayoutViewModel.dragOffset),
    setDragOffset: (Float) -> Unit = resizableLayoutViewModel::setDragOffset,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    ResizableLayout(
        availableWidth = maxWidth,
        baseWeight = 0.6f,
        minWeight = 0.4f,
        maxWeight = 1.25f,
        dragOffset = dragOffset,
        setDragOffset = setDragOffset,
        analyticsPrefix = "playbackSheet.layout",
        modifier = modifier,
    ) { resizableModifier ->
        val labelMod = Modifier.padding(top = AppTheme.specs.padding)
        LazyColumn(
            state = queueListState,
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colors.background)
        ) {
            playbackQueueLabel(resizableModifier.then(labelMod))

            if (playbackQueue.isLastAudio) {
                item {
                    Text(
                        text = stringResource(R.string.playback_queue_empty),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppTheme.specs.padding)
                    )
                }
            }

            playbackQueue(
                playbackQueue = playbackQueue,
                scrollToTop = scrollToTop,
                playbackConnection = playbackConnection,
            )
        }
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .then(resizableModifier)
        )
    }
}

@Composable
private fun PlaybackSheetTopBar(
    playbackQueue: PlaybackQueue,
    onClose: Callback,
    onTitleClick: Callback,
    onSaveQueueAsPlaylist: Callback,
) {
    TopAppBar(
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        title = { PlaybackSheetTopBarTitle(playbackQueue, onTitleClick) },
        actions = { PlaybackSheetTopBarActions(playbackQueue, onSaveQueueAsPlaylist) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                    modifier = Modifier.size(AppTheme.specs.iconSize),
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun PlaybackSheetTopBarTitle(
    playbackQueue: PlaybackQueue,
    onTitleClick: Callback,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .offset(x = -8.dp) // idk why this is needed for centering
            .simpleClickable(onClick = onTitleClick)
    ) {
        val context = LocalContext.current
        val queueTitle = playbackQueue.title.asQueueTitle()
        Text(
            text = queueTitle.localizeType(context.resources).uppercase(),
            style = MaterialTheme.typography.overline.copy(fontWeight = FontWeight.Light),
            maxLines = 1,
        )
        Text(
            text = queueTitle.localizeValue(),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
    }
}

@Composable
private fun PlaybackSheetTopBarActions(
    playbackQueue: PlaybackQueue,
    onSaveQueueAsPlaylist: Callback,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
        if (playbackQueue.isValid) {
            val (addToPlaylistVisible, setAddToPlaylistVisible) = remember { mutableStateOf(false) }
            val (addQueueToPlaylistVisible, setAddQueueToPlaylistVisible) = remember { mutableStateOf(false) }

            AddToPlaylistMenu(playbackQueue.currentAudio, addToPlaylistVisible, setAddToPlaylistVisible)
            AddToPlaylistMenu(playbackQueue.audios, addQueueToPlaylistVisible, setAddQueueToPlaylistVisible)

            AudioDropdownMenu(
                expanded = expanded,
                onExpandedChange = setExpanded,
                actionLabels = currentPlayingMenuActionLabels,
                extraActionLabels = listOf(AddQueueToPlaylist, SaveQueueAsPlaylist)
            ) { actionLabel ->
                val audio = playbackQueue.currentAudio
                when (val action = AudioItemAction.from(actionLabel, audio)) {
                    is AudioItemAction.AddToPlaylist -> setAddToPlaylistVisible(true)
                    else -> {
                        action.handleExtraActions(actionHandler) {
                            when (it.actionLabelRes) {
                                AddQueueToPlaylist -> setAddQueueToPlaylistVisible(true)
                                SaveQueueAsPlaylist -> onSaveQueueAsPlaylist()
                            }
                        }
                    }
                }
            }
        } else MoreVerticalIcon()
    }
}

@Composable
private fun PlaybackAudioInfo(audio: Audio, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dlItem = audio.audioDownloadItem
    if (dlItem != null) {
        val audiHeader = dlItem.audioHeader(context)
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = AppTheme.specs.padding)
        ) {
            Surface(
                color = MaterialTheme.colors.plainBackgroundColor().copy(alpha = 0.1f),
                shape = CircleShape,
            ) {
                Text(
                    text = audiHeader.info(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

private fun LazyListScope.playbackQueueLabel(modifier: Modifier = Modifier) {
    item {
        Row(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.playback_queue_title),
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(AppTheme.specs.padding)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
private fun LazyListScope.playbackQueue(
    playbackQueue: PlaybackQueue,
    scrollToTop: Callback,
    playbackConnection: PlaybackConnection,
) {
    val lastIndex = playbackQueue.audios.size
    val firstIndex = (playbackQueue.currentIndex + 1).coerceAtMost(lastIndex)
    val queue = playbackQueue.audios.subList(firstIndex, lastIndex)
    itemsIndexed(queue, key = { _, a -> a.primaryKey }) { index, audio ->
        val realPosition = firstIndex + index
        AudioRow(
            audio = audio,
            observeNowPlayingAudio = false,
            imageSize = 40.dp,
            onPlayAudio = {
                playbackConnection.transportControls?.skipToQueueItem(realPosition.toLong())
                scrollToTop()
            },
            extraActionLabels = listOf(RemoveFromPlaylist),
            onExtraAction = { playbackConnection.removeByPosition(realPosition) },
            modifier = Modifier.animateItemPlacement()
        )
    }
}
