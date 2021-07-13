/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import tm.alashow.ui.coloredShadow
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.topAppBarTitleStyle
import tm.alashow.ui.theme.topAppBarTitleStyleSmall
import tm.alashow.ui.theme.translucentSurface
import tm.alashow.ui.theme.translucentSurfaceColor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppTopBar(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .translucentSurface()
            .statusBarsPadding()
    ) {
        Text(
            title,
            style = topAppBarTitleStyle(),
            modifier = Modifier.padding(AppTheme.specs.padding)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CollapsingTopBar(
    title: String,
    collapsed: Boolean = true,
    onNavigationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appBarColor = translucentSurfaceColor()
    val backgroundColor = animateColorAsState(if (collapsed) appBarColor.copy(alpha = 0f) else appBarColor).value
    val contentColor = animateColorAsState(if (collapsed) Color.White else contentColorFor(backgroundColor)).value
    val contentShadow = if (collapsed) Modifier.coloredShadow(Color.Black) else Modifier

    TopAppBar(
        modifier = modifier,
        elevation = 0.dp,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = {
            AnimatedVisibility(visible = !collapsed, enter = fadeIn(), exit = fadeOut()) {
                Text(title, style = topAppBarTitleStyleSmall())
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    rememberVectorPainter(Icons.Filled.ArrowBack),
                    contentDescription = stringResource(R.string.generic_back),
                    modifier = contentShadow
                )
            }
        },
    )
}
