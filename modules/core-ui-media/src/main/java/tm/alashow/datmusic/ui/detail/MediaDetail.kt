/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import kotlin.math.round
import tm.alashow.base.util.extensions.Callback
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.base.util.extensions.orNA
import tm.alashow.datmusic.ui.components.CoverHeaderDefaults
import tm.alashow.datmusic.ui.components.CoverHeaderRow
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Success
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.components.CollapsingTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.fullScreenLoading
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme

typealias MediaDetails<DetailType> = LazyListScope.(details: Async<DetailType>, detailsLoading: Boolean) -> Boolean
typealias MediaDetailsFail<DetailType> = LazyListScope.(details: Async<DetailType>, onFailRetry: () -> Unit) -> Unit
typealias MediaDetailsEmpty<DetailType> = LazyListScope.(details: Async<DetailType>, detailsEmpty: Boolean, onEmptyRetry: () -> Unit) -> Unit

@Composable
fun <DetailType> MediaDetail(
    viewState: MediaDetailViewState<DetailType>,
    @StringRes titleRes: Int,
    onFailRetry: Callback,
    onEmptyRetry: Callback,
    onTitleClick: Callback = {},
    mediaDetails: MediaDetails<DetailType>,
    mediaDetailsFail: MediaDetailsFail<DetailType> = { a, b -> defaultMediaDetailsFail(a, b) },
    mediaDetailsEmpty: MediaDetailsEmpty<DetailType> = { a, b, c -> defaultMediaDetailsEmpty(a, b, c) },
    extraHeaderContent: @Composable ColumnScope.() -> Unit = {},
    navigator: Navigator = LocalNavigator.current,
) {
    val listState = rememberLazyListState()

    val headerHeight = CoverHeaderDefaults.height
    val headerOffsetProgress = remember { Animatable(0f) }

    OffsetNotifyingBox(headerHeight = headerHeight) { _, progress ->
        Scaffold(
            topBar = {
                LaunchedEffect(progress.value) {
                    headerOffsetProgress.animateTo(round(progress.value))
                }

                CollapsingTopBar(
                    title = stringResource(titleRes),
                    collapsed = headerOffsetProgress.value == 0f,
                    onNavigationClick = {
                        navigator.goBack()
                    },
                )
            }
        ) { padding ->
            MediaDetailContent(
                viewState = viewState,
                onFailRetry = onFailRetry,
                onEmptyRetry = onEmptyRetry,
                onTitleClick = onTitleClick,
                padding = padding,
                listState = listState,
                headerOffsetProgress = progress,
                mediaDetails = mediaDetails,
                mediaDetailsFail = mediaDetailsFail,
                mediaDetailsEmpty = mediaDetailsEmpty,
                extraHeaderContent = extraHeaderContent,
            )
        }
    }
}

@Composable
private fun <DetailType, T : MediaDetailViewState<DetailType>> MediaDetailContent(
    viewState: T,
    onFailRetry: Callback,
    onEmptyRetry: Callback,
    onTitleClick: Callback,
    listState: LazyListState,
    headerOffsetProgress: State<Float>,
    mediaDetails: MediaDetails<DetailType>,
    mediaDetailsFail: MediaDetailsFail<DetailType>,
    mediaDetailsEmpty: MediaDetailsEmpty<DetailType>,
    extraHeaderContent: @Composable ColumnScope.() -> Unit = {},
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val artwork = viewState.artwork(context)
    val adaptiveColor = adaptiveColor(
        artwork,
        fallback = MaterialTheme.colors.background,
        gradientEndColor = MaterialTheme.colors.background
    )
    val adaptiveBackground = Modifier.background(adaptiveColor.gradient)

    // apply adaptive background to whole list only on light theme
    // because full list gradient doesn't look great on dark
    val isLight = MaterialTheme.colors.isLight
    val listBackgroundMod = if (isLight) adaptiveBackground else Modifier
    val headerBackgroundMod = if (isLight) Modifier else adaptiveBackground

    LazyColumn(
        state = listState,
        modifier = listBackgroundMod.fillMaxSize(),
        contentPadding = PaddingValues(bottom = padding.calculateTopPadding() + padding.calculateBottomPadding())
    ) {
        if (viewState.isLoaded) {
            val details = viewState.details()
            val detailsLoading = details is Incomplete

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                    modifier = headerBackgroundMod
                        .padding(AppTheme.specs.padding)
                        .statusBarsPadding(),
                ) {
                    CoverHeaderRow(
                        title = viewState.title.orNA(),
                        imageData = artwork,
                        offsetProgress = headerOffsetProgress,
                        titleModifier = Modifier.simpleClickable(onClick = onTitleClick)
                    )
                    extraHeaderContent()
                }
            }

            val isEmpty = mediaDetails(details, detailsLoading)

            mediaDetailsFail(details, onFailRetry)
            mediaDetailsEmpty(details, isEmpty, onEmptyRetry)
        } else {
            fullScreenLoading()
        }
    }
}

private fun <T> LazyListScope.defaultMediaDetailsFail(
    details: Async<T>,
    onFailRetry: () -> Unit,
) {
    if (details is Fail) {
        item {
            ErrorBox(
                title = stringResource(details.error.localizedTitle()),
                message = stringResource(details.error.localizedMessage()),
                onRetryClick = onFailRetry,
                modifier = Modifier.fillParentMaxHeight(0.5f)
            )
        }
    }
}

private fun <T> LazyListScope.defaultMediaDetailsEmpty(
    details: Async<T>,
    detailsEmpty: Boolean,
    onEmptyRetry: () -> Unit,
) {
    if (details is Success && detailsEmpty) {
        item {
            EmptyErrorBox(
                onRetryClick = onEmptyRetry,
                modifier = Modifier.fillParentMaxHeight(0.5f)
            )
        }
    }
}
