/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import tm.alashow.ui.theme.topAppBarTitleStyleSmall
import tm.alashow.ui.theme.translucentSurfaceColor

@Composable
fun DetailScreenAppBar(
    title: String,
    onNavigationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        elevation = 0.dp,
        backgroundColor = translucentSurfaceColor(),
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = {
            Text(title, style = topAppBarTitleStyleSmall())
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    rememberVectorPainter(Icons.Filled.ArrowBack),
                    contentDescription = stringResource(R.string.generic_back)
                )
            }
        },
    )
}
