/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.math.round
import tm.alashow.base.util.click
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.getNavArgument
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.navigation.QUERY_KEY
import tm.alashow.ui.OffsetNotifyingBox
import tm.alashow.ui.components.ChipsRow
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.borderlessTextFieldColors
import tm.alashow.ui.theme.topAppBarTitleStyle
import tm.alashow.ui.theme.translucentSurface

@Composable
fun Search() {
    Search(viewModel = hiltViewModel())
}

@Composable
internal fun Search(
    viewModel: SearchViewModel,
) {
    Search(viewModel) { action ->
        viewModel.submitAction(action)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun Search(
    viewModel: SearchViewModel,
    actioner: (SearchAction) -> Unit
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = SearchViewState.Empty)
    val listState = rememberLazyListState()

    Search(viewState, actioner, viewModel, listState)
}

@Composable
private fun Search(
    viewState: SearchViewState,
    actioner: (SearchAction) -> Unit,
    viewModel: SearchViewModel,
    listState: LazyListState
) {
    val searchBarHideThreshold = 4
    val searchBarHeight = 200.dp
    val searchBarOffset = remember { Animatable(0f) }

    OffsetNotifyingBox(headerHeight = searchBarHeight) { _, progress ->
        Scaffold(
            topBar = {
                LaunchedEffect(progress.value, listState.firstVisibleItemIndex) {
                    if (listState.firstVisibleItemIndex > searchBarHideThreshold) {
                        // rounding is important here because we don't searchBar to be stuck in between transitions
                        searchBarOffset.animateTo(round(progress.value))
                    } else searchBarOffset.animateTo(0f)
                }

                SearchAppBar(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = 1 - searchBarOffset.value
                            translationY = searchBarHeight.value * (-searchBarOffset.value)
                        },
                    state = viewState,
                    onQueryChange = { actioner(SearchAction.QueryChange(it)) },
                    onSearch = { actioner(SearchAction.Search) },
                    onBackendTypeSelect = { actioner(it) }
                )
            }
        ) {
            SearchList(
                viewModel = viewModel,
                listState = listState,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
private fun SearchAppBar(
    state: SearchViewState,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onBackendTypeSelect: (SearchAction.SelectBackendType) -> Unit = {}
) {
    val initialQuery = (getNavArgument(QUERY_KEY) ?: "").toString()

    Box(
        modifier = modifier
            .translucentSurface()
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = AppTheme.specs.paddingTiny)
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val hasWindowFocus = LocalWindowInfo.current.isWindowFocused
        val keyboardVisible = LocalWindowInsets.current.ime.isVisible

        var focused by remember { mutableStateOf(false) }
        val searchActive = focused && hasWindowFocus && keyboardVisible

        val triggerSearch = {
            onSearch()
            keyboardController?.hide()
            focusManager.clearFocus()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
            modifier = Modifier.animateContentSize()
        ) {
            // hide title bar if we can make search list not jump during transitions caused by toolbar height change
            // if (!searchActive)
            Text(
                text = stringResource(R.string.search_title),
                style = topAppBarTitleStyle(),
                modifier = titleModifier.padding(start = AppTheme.specs.padding, top = AppTheme.specs.padding),
            )

            var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(initialQuery)) }
            SearchTextField(
                value = query,
                onValueChange = { value ->
                    query = value
                    onQueryChange(value.text)
                },
                onSearch = { triggerSearch() },
                hint = if (!searchActive) stringResource(R.string.search_hint) else stringResource(R.string.search_hint_query),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
            )

            var backends = state.searchFilter.backends
            // this applies until default selections mean everything is chosen
            if (backends == SearchFilter.DefaultBackends)
                backends = emptySet()

            val filterVisible = searchActive || query.text.isNotBlank() || backends.isNotEmpty()
            SearchFilterPanel(visible = filterVisible, backends) { selectAction ->
                onBackendTypeSelect(selectAction)
                triggerSearch()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColumnScope.SearchFilterPanel(
    visible: Boolean,
    selectedItems: Set<DatmusicSearchParams.BackendType>,
    onBackendTypeSelect: (SearchAction.SelectBackendType) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandIn(Alignment.TopCenter) + fadeIn(),
        exit = shrinkOut(Alignment.BottomCenter) + fadeOut()
    ) {
        ChipsRow(
            items = DatmusicSearchParams.BackendType.values().toList(),
            selectedItems = selectedItems,
            onItemSelect = { selected, item ->
                onBackendTypeSelect(SearchAction.SelectBackendType(selected, item))
            },
            labelMapper = {
                stringResource(
                    when (it) {
                        DatmusicSearchParams.BackendType.AUDIOS -> R.string.search_audios
                        DatmusicSearchParams.BackendType.ARTISTS -> R.string.search_artists
                        DatmusicSearchParams.BackendType.ALBUMS -> R.string.search_albums
                        DatmusicSearchParams.BackendType.MINERVA -> R.string.search_minerva
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    hint: String,
    maxLength: Int = 50,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search, keyboardType = KeyboardType.Text),
    keyboardActions: KeyboardActions = KeyboardActions(onSearch = { onSearch() }),
    analytics: FirebaseAnalytics = LocalAnalytics.current
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.text.length <= maxLength) onValueChange(it) },
        placeholder = { Text(text = hint) },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.text.isNotEmpty(),
                enter = expandIn(Alignment.Center),
                exit = shrinkOut(Alignment.Center)
            ) {
                IconButton(
                    onClick = {
                        onValueChange(TextFieldValue())
                        analytics.click("search.clear")
                    },
                ) {
                    Icon(
                        tint = MaterialTheme.colors.secondary,
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.generic_clear)
                    )
                }
            }
        },
        colors = borderlessTextFieldColors(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        maxLines = 1,
        visualTransformation = { text -> TransformedText(text.capitalize(), OffsetMapping.Identity) },
        modifier = modifier
            .padding(horizontal = AppTheme.specs.padding)
            .background(AppTheme.colors.onSurfaceInputBackground, MaterialTheme.shapes.small)
    )
}
