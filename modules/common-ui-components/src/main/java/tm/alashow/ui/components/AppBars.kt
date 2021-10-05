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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
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

private val AppBarHorizontalPadding = 4.dp

private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)
private val TitleIconModifier = Modifier
    .fillMaxHeight()
    .width(72.dp - AppBarHorizontalPadding)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String,
    titleContent: @Composable () -> Unit = {
        Text(
            title,
            style = topAppBarTitleStyle()
        )
    },
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    actionsContentAlpha: Float = ContentAlpha.medium
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .translucentSurface()
            .statusBarsPadding()
            .padding(vertical = AppTheme.specs.padding)
            .padding(end = AppTheme.specs.padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon == null) {
            Spacer(TitleInsetWithoutIcon)
        } else {
            Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = navigationIcon
                )
            }
        }

        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.h6) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = titleContent
                )
            }
        }

        CompositionLocalProvider(LocalContentAlpha provides actionsContentAlpha) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CollapsingTopBar(
    title: String,
    modifier: Modifier = Modifier,
    collapsed: Boolean = true,
    onNavigationClick: () -> Unit = {},
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
