/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dagger.hilt.android.AndroidEntryPoint
import tm.alashow.common.compose.Scaffold
import tm.alashow.datmusic.ui.search.SearchAppBar
import tm.alashow.datmusic.ui.theme.AppBarAlphas
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.ContentPadding
import tm.alashow.datmusic.ui.theme.ContentPaddingSmall
import tm.alashow.datmusic.ui.theme.parseColor

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

@Preview
@Composable
private fun App() {
    val viewModel: MainViewModel = hiltViewModel()
    AppTheme {
        ProvideWindowInsets(consumeWindowInsets = false) {
            Scaffold(
                bottomBar = {
                    HomeBottomNavigation()
                }
            ) {
                val state by viewModel.state.collectAsState(initial = MainViewState.Empty)
                Screen(state)
            }
        }
    }
}

@Composable
private fun Screen(state: MainViewState) {
    Scaffold(
        topBar = {
            ScreenAppBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            items(500) { index ->
                Row(Modifier.padding(ContentPadding)) {
                    val image = rememberCoilPainter("https://source.unsplash.com/100x100", fadeIn = true)
                    Image(
                        painter = image,
                        contentDescription = null,
                        Modifier
                            .size(60.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Spacer(Modifier.width(ContentPadding))
                    Column(verticalArrangement = Arrangement.spacedBy(ContentPaddingSmall)) {
                        Text("Title")
                        Text("Artist")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenAppBar(
    modifier: Modifier = Modifier
) {
    SearchAppBar()
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
