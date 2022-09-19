/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.LocalSnackbarHostState
import tm.alashow.common.compose.copy
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.downloader.audioHeader
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.artwork
import tm.alashow.datmusic.playback.isIdle
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.QueueTitle.Companion.asQueueTitle
import tm.alashow.datmusic.ui.audios.AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM
import tm.alashow.datmusic.ui.audios.AudioActionHandler
import tm.alashow.datmusic.ui.audios.AudioDropdownMenu
import tm.alashow.datmusic.ui.audios.AudioItemAction
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.datmusic.ui.audios.LocalAudioActionHandler
import tm.alashow.datmusic.ui.audios.audioActionHandler
import tm.alashow.datmusic.ui.audios.currentPlayingMenuActionLabels
import tm.alashow.datmusic.ui.library.playlist.addTo.AddToPlaylistMenu
import tm.alashow.datmusic.ui.playback.components.PlaybackArtworkPagerWithNowPlayingAndControls
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.activityHiltViewModel
import tm.alashow.ui.ADAPTIVE_COLOR_ANIMATION
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.ResizableLayout
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.MoreVerticalIcon
import tm.alashow.ui.contentColor
import tm.alashow.ui.isWideLayout
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.LocalAdaptiveColor
import tm.alashow.ui.theme.LocalThemeState
import tm.alashow.ui.theme.Red
import tm.alashow.ui.theme.Theme
import tm.alashow.ui.theme.plainBackgroundColor

private val RemoveFromPlaylist = R.string.playback_queue_removeFromQueue
private val AddQueueToPlaylist = R.string.playback_queue_addQueueToPlaylist
private val SaveQueueAsPlaylist = R.string.playback_queue_saveAsPlaylist

private val PlaybackSheetThemeState
    @Composable
    get() = LocalThemeState.current.let {
        when {
            it.colorPalettePreference.isDynamic -> it
            else -> it.copy(colorPalettePreference = ColorPalettePreference.Black)
        }
    }

@Composable
fun PlaybackSheetRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> PlaybackSheetPreview()
        else -> PlaybackSheet()
    }
}

@Composable
internal fun PlaybackSheet(
    sheetTheme: ThemeState = PlaybackSheetThemeState,
    navigator: Navigator = LocalNavigator.current,
    viewModel: PlaybackViewModel = activityHiltViewModel(),
) {
    val listState = rememberLazyListState()

    val coroutine = rememberCoroutineScope()
    val scrollToTop = {
        coroutine.launch {
            listState.animateScrollToItem(0)
        }
        Unit
    }

    val audioActionHandler = audioActionHandler()
    CompositionLocalProvider(LocalAudioActionHandler provides audioActionHandler) {
        AppTheme(theme = sheetTheme, changeSystemBar = false) {
            PlaybackSheet(
                onClose = { navigator.goBack() },
                scrollToTop = scrollToTop,
                listState = listState,
                queueListState = rememberLazyListState(),
                onSaveQueueAsPlaylist = viewModel::onSaveQueueAsPlaylist,
                onNavigateToQueueSource = viewModel::onNavigateToQueueSource,
                onTitleClick = viewModel::onTitleClick,
                onArtistClick = viewModel::onArtistClick,
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PlaybackSheet(
    onClose: () -> Unit,
    scrollToTop: () -> Unit,
    onSaveQueueAsPlaylist: () -> Unit,
    onNavigateToQueueSource: () -> Unit,
    onTitleClick: () -> Unit,
    onArtistClick: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    queueListState: LazyListState = rememberLazyListState(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
    val pagerState = rememberPagerState(playbackQueue.currentIndex)

    val adaptiveColor by adaptiveColor(
        nowPlaying.artwork,
        initial = MaterialTheme.colorScheme.onBackground,
        gradientEndColor = MaterialTheme.colorScheme.background,
    )
    val contentColor by animateColorAsState(adaptiveColor.color, ADAPTIVE_COLOR_ANIMATION)

    LaunchedEffect(playbackConnection) {
        playbackConnection.playbackState
            .filter { it != NONE_PLAYBACK_STATE }
            .collectLatest { if (it.isIdle) onClose() }
    }

    if (playbackState == NONE_PLAYBACK_STATE) {
        Row(Modifier.fillMaxSize()) { FullScreenLoading(delayMillis = 0) }
        return
    }

    CompositionLocalProvider(LocalAdaptiveColor provides adaptiveColor) {
        MaterialTheme(Theme.colorScheme.copy(surfaceTint = LocalAdaptiveColor.current.color)) {
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
                        containerColor = Color.Transparent,
                        contentColor = Theme.colorScheme.onSurface,
                        snackbarHost = {
                            DismissableSnackbarHost(
                                snackbarHostState,
                                modifier = Modifier.navigationBarsPadding()
                            )
                        },
                        modifier = Modifier
                            .background(adaptiveColor.gradient)
                            .weight(1f)
                    ) { paddings ->
                        LazyColumn(
                            state = listState,
                            contentPadding = paddings.copy(top = 0.dp),
                        ) {
                            item {
                                PlaybackSheetTopBar(
                                    playbackQueue = playbackQueue,
                                    onClose = onClose,
                                    onTitleClick = onNavigateToQueueSource,
                                    onSaveQueueAsPlaylist = onSaveQueueAsPlaylist,
                                )
                            }
                            item {
                                PlaybackArtworkPagerWithNowPlayingAndControls(
                                    nowPlaying = nowPlaying,
                                    playbackState = playbackState,
                                    pagerState = pagerState,
                                    contentColor = contentColor,
                                    onTitleClick = onTitleClick,
                                    onArtistClick = onArtistClick,
                                    artworkVerticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillParentMaxHeight(fraction = 0.75f),
                                )
                            }

                            if (playbackQueue.isValid)
                                item {
                                    PlaybackAudioInfo(playbackQueue.currentAudio)
                                }

                            if (!isWideLayout && !playbackQueue.isLastAudio) {
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
    }
}

@Composable
private fun RowScope.ResizablePlaybackQueue(
    maxWidth: Dp,
    playbackQueue: PlaybackQueue,
    scrollToTop: () -> Unit,
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
        initialWeight = 0.6f,
        minWeight = 0.4f,
        maxWeight = 1.25f,
        dragOffset = dragOffset,
        setDragOffset = setDragOffset,
        analyticsPrefix = "playbackSheet.layout",
        modifier = modifier,
    ) { resizableModifier ->
        val labelMod = Modifier.padding(top = AppTheme.specs.padding)
        Surface {
            LazyColumn(
                state = queueListState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxHeight()
            ) {
                playbackQueueLabel(resizableModifier.then(labelMod))

                if (playbackQueue.isLastAudio) {
                    item {
                        Text(
                            text = stringResource(R.string.playback_queue_empty),
                            style = MaterialTheme.typography.bodyLarge,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackSheetTopBar(
    playbackQueue: PlaybackQueue,
    onClose: () -> Unit,
    onTitleClick: () -> Unit,
    onSaveQueueAsPlaylist: () -> Unit,
) {
    // TODO: Remove after https://android-review.googlesource.com/c/platform/frameworks/support/+/2209896/ is available
    // override colorScheme for TopAppBar so we can make the background transparent
    // but revert it back for actions since PlaybackSheetTopBarActions uses a Surface
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme.copy(surface = Color.Transparent)) {
        TopAppBar(
            title = { PlaybackSheetTopBarTitle(playbackQueue, onTitleClick) },
            actions = {
                MaterialTheme(colorScheme) {
                    PlaybackSheetTopBarActions(playbackQueue, onSaveQueueAsPlaylist)
                }
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                        modifier = Modifier.size(AppTheme.specs.iconSize),
                        contentDescription = null,
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun PlaybackSheetTopBarTitle(
    playbackQueue: PlaybackQueue,
    onTitleClick: () -> Unit,
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
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light),
            maxLines = 1,
        )
        val titleValue = queueTitle.localizeValue()
        if (titleValue.isNotBlank()) { // TODO: Remove when https://issuetracker.google.com/issues/245209981 is fixed
            Text(
                text = titleValue,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun PlaybackSheetTopBarActions(
    playbackQueue: PlaybackQueue,
    onSaveQueueAsPlaylist: () -> Unit,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    ProvideContentAlpha(ContentAlpha.high) {
        if (playbackQueue.isValid) {
            val (addToPlaylistVisible, setAddToPlaylistVisible) = remember { mutableStateOf(false) }
            val (addQueueToPlaylistVisible, setAddQueueToPlaylistVisible) = remember { mutableStateOf(false) }

            AddToPlaylistMenu(playbackQueue.currentAudio, addToPlaylistVisible, setAddToPlaylistVisible)
            AddToPlaylistMenu(playbackQueue, addQueueToPlaylistVisible, setAddQueueToPlaylistVisible)

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
                color = plainBackgroundColor().copy(alpha = 0.1f),
                shape = CircleShape,
            ) {
                Text(
                    text = audiHeader.info(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(AppTheme.specs.padding)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.playbackQueue(
    playbackQueue: PlaybackQueue,
    scrollToTop: () -> Unit,
    playbackConnection: PlaybackConnection,
) {
    val lastIndex = playbackQueue.lastIndex
    val firstIndex = (playbackQueue.currentIndex + 1).coerceAtMost(lastIndex)
    val queue = playbackQueue.subList(firstIndex, lastIndex)
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
            hasAddToPlaylistSwipeAction = false,
            extraEndSwipeActions = listOf(
                removeAudioFromQueueSwipeAction(
                    onRemoveFromPlaylist = { playbackConnection.removeByPosition(realPosition) }
                )
            ),
            modifier = Modifier.animateItemPlacement()
        )
    }
}

@Composable
private fun removeAudioFromQueueSwipeAction(
    onRemoveFromPlaylist: () -> Unit,
    backgroundColor: Color = Red,
) = SwipeAction(
    background = backgroundColor,
    weight = AUDIO_SWIPE_ACTION_WEIGHT_MEDIUM,
    icon = {
        Icon(
            modifier = Modifier.padding(AppTheme.specs.padding),
            painter = rememberVectorPainter(Icons.Default.PlaylistRemove),
            tint = backgroundColor.contentColor(),
            contentDescription = null
        )
    },
    onSwipe = onRemoveFromPlaylist,
    isUndo = false,
)

@CombinedPreview
@Composable
fun PlaybackSheetPreview() = PreviewDatmusicCore {
    AppTheme(theme = PlaybackSheetThemeState) {
        PlaybackSheet(
            onClose = {},
            scrollToTop = {},
            onSaveQueueAsPlaylist = {},
            onNavigateToQueueSource = {},
            onTitleClick = {},
            onArtistClick = {},
        )
    }
}
