/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import dagger.hilt.android.AndroidEntryPoint
import tm.alashow.common.compose.Scaffold
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.components.SelectableDropdownMenu
import tm.alashow.datmusic.ui.search.SearchAppBar
import tm.alashow.datmusic.ui.theme.AppBarAlphas
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.ColorPalettePreference
import tm.alashow.datmusic.ui.theme.DarkModePreference
import tm.alashow.datmusic.ui.theme.DefaultTheme
import tm.alashow.datmusic.ui.theme.ThemeState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            App()
        }
    }
}

@Composable
private fun App() {
    val viewModel: MainViewModel = hiltViewModel()

    val (themeState, setThemeState) = rememberSaveable { mutableStateOf(DefaultTheme) }

    AppTheme(themeState) {
        ProvideWindowInsets(consumeWindowInsets = false) {
            Scaffold(
                bottomBar = {
                    HomeBottomNavigation()
                }
            ) {
                Screen(viewModel, themeState, setThemeState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Screen(viewModel: MainViewModel, themeState: ThemeState, setThemeState: (ThemeState) -> Unit) {
    Scaffold(
        topBar = {
            SearchAppBar(
                onSearchQueryChange = { query ->
                    viewModel.submitAction(SearchAction.Search(query))
                }
            )
        }
    ) { paddingValues ->
        val uiSettingsVisible = false
        if (uiSettingsVisible)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = paddingValues,
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(AppTheme.specs.paddings)
                            .fillMaxWidth()
                    ) {
                        Text("Dark mode")
                        SelectableDropdownMenu(
                            items = DarkModePreference.values().toList(),
                            selectedItem = themeState.darkModePreference,
                            onItemSelect = { setThemeState(themeState.copy(darkModePreference = it)) }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(AppTheme.specs.paddings)
                            .fillMaxWidth()
                    ) {
                        Text("Color palette")
                        SelectableDropdownMenu(
                            items = ColorPalettePreference.values().toList(),
                            selectedItem = themeState.colorPalettePreference,
                            onItemSelect = { setThemeState(themeState.copy(colorPalettePreference = it)) }
                        )
                    }
                }
            }
        ArtistList(viewModel, paddingValues)
        // AudioList(viewModel, paddingValues)
    }
}

@Composable
private fun AudioList(viewModel: MainViewModel, paddingValues: PaddingValues) {
    EntityList(
        lazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedAudioList).collectAsLazyPagingItems(),
        paddingValues = paddingValues
    ) {
        val audio = it ?: return@EntityList

        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppTheme.specs.padding)
        ) {
            val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
            Image(
                painter = image,
                contentDescription = null,
                Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .placeholder(
                        visible = image.loadState is ImageLoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Spacer(Modifier.width(AppTheme.specs.padding))
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
                Text(audio.title)
                Text(audio.artist)
            }
        }
    }
}

@Composable
private fun ArtistList(viewModel: MainViewModel, paddingValues: PaddingValues) {
    EntityListRow(
        lazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedArtistsList).collectAsLazyPagingItems(),
        paddingValues = paddingValues
    ) {
        val audio = it ?: return@EntityListRow

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.specs.padding)
        ) {
            val image = rememberCoilPainter(audio.photo?.url, fadeIn = true)
            Image(
                painter = image,
                contentDescription = null,
                Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .placeholder(
                        visible = image.loadState is ImageLoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Text(audio.name)
        }
    }
}

@Composable
internal fun HomeBottomNavigation(
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = AppBarAlphas.translucentBarAlpha()),
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        elevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            HomeBottomNavigationItem(
                label = "Tab 1",
                contentDescription = "Tab 1",
                selected = true,
                onClick = { },
                painter = rememberVectorPainter(Icons.Default.Search),
            )
            HomeBottomNavigationItem(
                label = "Tab 2",
                contentDescription = "Tab 2",
                selected = false,
                onClick = { },
                painter = rememberVectorPainter(Icons.Default.Edit),
            )
        }
    }
}

@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    selectedPainter: Painter? = null,
    painter: Painter,
    contentDescription: String,
    label: String,
    onClick: () -> Unit,
) {
    BottomNavigationItem(
        icon = {
            if (selectedPainter != null) {
                Crossfade(targetState = selected) { selected ->
                    Icon(
                        painter = if (selected) selectedPainter else painter,
                        contentDescription = contentDescription
                    )
                }
            } else {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}
