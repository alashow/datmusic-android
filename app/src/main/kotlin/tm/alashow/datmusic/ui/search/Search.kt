/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.ui.components.ChipsRow
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.borderlessTextFieldColors
import tm.alashow.datmusic.ui.theme.topAppBarTitleStyle
import tm.alashow.datmusic.ui.theme.translucentSurface

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
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = SearchViewState.Empty)
    val listState = rememberLazyListState()
    val collapsingToolbarState = rememberCollapsingToolbarScaffoldState()

    Scaffold { padding ->
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            scrollStrategy = ScrollStrategy.EnterAlways,
            state = collapsingToolbarState,
            toolbar = {
                SearchAppBar(
                    state = viewState,
                    onQueryChange = { actioner(SearchAction.QueryChange(it)) },
                    onSearch = { actioner(SearchAction.Search) },
                    onBackendTypeSelect = { actioner(it) }
                )
            }
        ) {

            SearchList(
                viewModel = viewModel,
                padding = padding,
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
    Box(
        modifier = modifier
            .translucentSurface()
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val hasWindowFocus = LocalWindowInfo.current.isWindowFocused
        val keyboardVisible = LocalWindowInsets.current.ime.isVisible

        var focused by remember { mutableStateOf(false) }
        val searchActive = focused && hasWindowFocus && keyboardVisible

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

            var queryValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

            SearchTextField(
                value = queryValue,
                onValueChange = { value ->
                    queryValue = value
                    onQueryChange(value.text)
                },
                onSearch = {
                    onSearch()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                hint = if (!searchActive) stringResource(R.string.search_hint) else stringResource(R.string.search_hint_query),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
            )

            var backends = state.searchFilter.backends
            // this applies until default selections mean everything is chosen
            if (backends == SearchFilter.DefaultBackends)
                backends = emptySet()

            val filterVisible = searchActive || queryValue.text.isNotBlank() || backends.isNotEmpty()
            SearchFilterPanel(visible = filterVisible, backends) { selectAction ->
                onBackendTypeSelect(selectAction)
                onSearch()
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
    onSearch: () -> Unit = {},
    hint: String,
    maxLength: Int = 50,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search, keyboardType = KeyboardType.Text),
    keyboardActions: KeyboardActions = KeyboardActions(onSearch = { onSearch() }),
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
                    onClick = { onValueChange(TextFieldValue()) },
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
