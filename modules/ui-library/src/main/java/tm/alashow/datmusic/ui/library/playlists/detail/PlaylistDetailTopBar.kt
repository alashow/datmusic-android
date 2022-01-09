/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tm.alashow.base.util.asString
import tm.alashow.base.util.extensions.muteUntil
import tm.alashow.datmusic.data.observers.playlist.PlaylistItemSortOption
import tm.alashow.datmusic.ui.detail.MediaDetailTopBar
import tm.alashow.datmusic.ui.library.R
import tm.alashow.ui.LifecycleRespectingBackHandler
import tm.alashow.ui.components.*
import tm.alashow.ui.theme.AppTheme

class PlaylistDetailTopBar(
    private val filterVisible: Boolean,
    private val setFilterVisible: (Boolean) -> Unit,
    private val hasSortingOption: Boolean,
    private val sortOptions: List<PlaylistItemSortOption>,
    private val sortOption: PlaylistItemSortOption,
    private val onSortOptionSelect: (PlaylistItemSortOption) -> Unit,
    private val onSearchQueryChange: (String) -> Unit = {},
    private val onClearFilter: () -> Unit = {}
) : MediaDetailTopBar() {

    @Composable
    override operator fun invoke(
        title: String,
        collapsedProgress: State<Float>,
        onGoBack: () -> Unit,
    ) {
        val coroutine = rememberCoroutineScope()
        var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
        val onQueryChange = { query: TextFieldValue ->
            searchQuery = query
            onSearchQueryChange(query.text)
        }
        val isCollapsedProgress by derivedStateOf {
            if (filterVisible) 1f
            else collapsedProgress.value.muteUntil(0.9f)
        }
        AppTopBar(
            title = title,
            collapsedProgress = isCollapsedProgress,
            navigationIcon = { AppBarNavigationIcon(onClick = onGoBack) },
            filterVisible = filterVisible,
            filterContent = {
                PlaylistDetailFilters(
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onClose = {
                        setFilterVisible(false)
                        onQueryChange(TextFieldValue())
                        coroutine.launch { delay(10); onQueryChange(TextFieldValue()) }
                    },
                    hasSortingOption = hasSortingOption,
                    sortOptions = sortOptions,
                    sortOption = sortOption,
                    onSortOptionSelect = onSortOptionSelect,
                )
            },
            actions = {
                IconButton(
                    onClick = { setFilterVisible(true) },
                    onLongClick = onClearFilter,
                    onLongClickLabel = stringResource(R.string.playlist_detail_filter_clear),
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(AppTheme.specs.iconSizeSmall),
                    )
                }
            }
        )
    }

    @Composable
    fun PlaylistDetailFilters(
        searchQuery: TextFieldValue,
        onQueryChange: (TextFieldValue) -> Unit,
        onClose: () -> Unit,
        hasSortingOption: Boolean,
        sortOptions: List<PlaylistItemSortOption>,
        sortOption: PlaylistItemSortOption,
        onSortOptionSelect: (PlaylistItemSortOption) -> Unit,
        modifier: Modifier = Modifier,
        context: Context = LocalContext.current,
    ) {
        LifecycleRespectingBackHandler(onBack = onClose)
        Row(
            Modifier.padding(
                end = AppTheme.specs.padding,
                bottom = AppTheme.specs.paddingTiny
            ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppBarNavigationIcon(onClick = onClose, Modifier.weight(1f))
            SearchTextField(
                modifier = Modifier.weight(4f),
                value = searchQuery,
                onValueChange = onQueryChange,
                hint = stringResource(R.string.playlist_detail_filter_search_hint),
                autoFocus = true,
            )
            Spacer(Modifier.width(AppTheme.specs.padding))
            SelectableDropdownMenu(
                items = sortOptions,
                selectedItem = sortOption,
                onItemSelect = onSortOptionSelect,
                border = ButtonDefaults.outlinedBorder,
                iconOnly = true,
                leadingIcon = Icons.Default.Sort,
                leadingIconColor = if (hasSortingOption) MaterialTheme.colors.secondary else LocalContentColor.current,
                itemLabelMapper = { it.asString(context) },
                itemSuffixMapper = {
                    if (it == sortOption) {
                        Spacer(Modifier.width(AppTheme.specs.paddingLarge))
                        Icon(
                            rememberVectorPainter(
                                if (it.isDescending) Icons.Default.ArrowDownward
                                else Icons.Default.ArrowUpward
                            ),
                            modifier = Modifier.size(14.dp),
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    }
}
