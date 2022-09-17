/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tm.alashow.base.util.extensions.orNA
import tm.alashow.domain.models.Incomplete
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.drawVerticalScrollbar
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.LocalAdaptiveColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <DetailType> MediaDetail(
    viewState: MediaDetailViewState<DetailType>,
    @StringRes titleRes: Int,
    onTitleClick: () -> Unit = {},
    onFailRetry: () -> Unit,
    onEmptyRetry: () -> Unit,
    mediaDetailContent: MediaDetailContent<DetailType>,
    mediaDetailTopBar: MediaDetailTopBar = MediaDetailTopBar(),
    mediaDetailHeader: MediaDetailHeader = MediaDetailHeader(),
    mediaDetailFail: MediaDetailFail<DetailType> = MediaDetailFail(),
    mediaDetailEmpty: MediaDetailEmpty<DetailType> = MediaDetailEmpty(),
    headerCoverIcon: VectorPainter? = null,
    scrollbarsEnabled: Boolean = false,
    isHeaderVisible: Boolean = true,
    extraHeaderContent: (@Composable ColumnScope.() -> Unit)? = null,
    navigator: Navigator = LocalNavigator.current,
) {
    val listState = rememberLazyListState()
    val headerOffsetProgress = coverHeaderScrollProgress(listState)
    Scaffold(
        topBar = {
            mediaDetailTopBar(
                title = viewState.title ?: stringResource(titleRes),
                collapsedProgress = headerOffsetProgress,
                onGoBack = navigator::goBack,
            )
        }
    ) { paddings ->
        MediaDetailContent(
            viewState = viewState,
            onFailRetry = onFailRetry,
            onEmptyRetry = onEmptyRetry,
            onTitleClick = onTitleClick,
            listState = listState,
            mediaDetailHeader = mediaDetailHeader,
            mediaDetailContent = mediaDetailContent,
            mediaDetailFail = mediaDetailFail,
            mediaDetailEmpty = mediaDetailEmpty,
            headerCoverIcon = headerCoverIcon,
            isHeaderVisible = isHeaderVisible,
            scrollbarsEnabled = scrollbarsEnabled,
            extraHeaderContent = extraHeaderContent,
            paddings = paddings,
        )
    }
}

@Composable
private fun <DetailType, T : MediaDetailViewState<DetailType>> MediaDetailContent(
    viewState: T,
    onFailRetry: () -> Unit,
    onEmptyRetry: () -> Unit,
    onTitleClick: () -> Unit,
    listState: LazyListState,
    mediaDetailHeader: MediaDetailHeader,
    mediaDetailContent: MediaDetailContent<DetailType>,
    mediaDetailFail: MediaDetailFail<DetailType>,
    mediaDetailEmpty: MediaDetailEmpty<DetailType>,
    headerCoverIcon: VectorPainter? = null,
    isHeaderVisible: Boolean = true,
    scrollbarsEnabled: Boolean,
    extraHeaderContent: (@Composable ColumnScope.() -> Unit)? = null,
    paddings: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val artwork = viewState.artwork(context)

    if (viewState.isLoaded) {
        val adaptiveColor by adaptiveColor(
            artwork,
            initial = MaterialTheme.colorScheme.background,
            fallback = MaterialTheme.colorScheme.secondary,
            gradientEndColor = MaterialTheme.colorScheme.background
        )
        val adaptiveBackground = Modifier.background(adaptiveColor.gradient)

        // apply adaptive background to whole list only on light theme
        // because full list gradient doesn't look great on dark
        val isLight = AppTheme.colors.isLightTheme
        val listBackgroundMod = if (isLight) adaptiveBackground else Modifier
        val headerBackgroundMod = if (isLight) Modifier else adaptiveBackground

        val topPadding = if (isHeaderVisible) 0.dp else paddings.calculateTopPadding()
        val bottomPadding = (if (isHeaderVisible) paddings.calculateTopPadding() else 0.dp) + paddings.calculateBottomPadding()

        CompositionLocalProvider(LocalAdaptiveColor provides adaptiveColor) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = topPadding,
                    bottom = bottomPadding,
                ),
                modifier = listBackgroundMod
                    .fillMaxSize()
                    .drawVerticalScrollbar(listState),
            ) {
                val details = viewState.details()
                val detailsLoading = details is Incomplete

                if (isHeaderVisible)
                    mediaDetailHeader(
                        list = this,
                        listState = listState,
                        headerBackgroundMod = headerBackgroundMod,
                        title = viewState.title.orNA(),
                        artwork = artwork,
                        onTitleClick = onTitleClick,
                        headerCoverIcon = headerCoverIcon,
                        extraHeaderContent = extraHeaderContent,
                    )

                val isEmpty = mediaDetailContent(
                    list = this,
                    details = details,
                    detailsLoading = detailsLoading
                )

                mediaDetailFail(
                    list = this,
                    details = details,
                    onFailRetry = onFailRetry
                )

                mediaDetailEmpty(
                    list = this,
                    details = details,
                    isHeaderVisible = isHeaderVisible,
                    detailsEmpty = isEmpty,
                    onEmptyRetry = onEmptyRetry
                )
            }
        }
    } else FullScreenLoading()
}
