/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.topAppBarTitleStyle
import tm.alashow.ui.theme.topAppBarTitleStyleSmall
import tm.alashow.ui.theme.translucentSurface
import tm.alashow.ui.theme.translucentSurfaceColor

val AppBarHeight = 56.dp
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
    collapsed: Float = 1f,
    onNavigationClick: () -> Unit = {},
) {
    val appBarColor = translucentSurfaceColor()
    val backgroundColor = appBarColor.copy(alpha = collapsed)
    val contentColor = contentColorFor(backgroundColor)

    TopAppBar(
        modifier = modifier,
        elevation = 0.dp,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = {
            Text(title, style = topAppBarTitleStyleSmall(), modifier = Modifier.alpha(collapsed))
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    rememberVectorPainter(Icons.Filled.ArrowBack),
                    contentDescription = stringResource(R.string.generic_back),
                )
            }
        },
    )
}
