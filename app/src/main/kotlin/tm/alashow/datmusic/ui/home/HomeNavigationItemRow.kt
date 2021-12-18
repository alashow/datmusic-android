/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun HomeNavigationItemRow(
    item: HomeNavigationItem,
    selected: Boolean,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = MaterialTheme.colors.secondary,
    unselectedContentColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = true,
                    color = selectedContentColor
                )
            )
            .padding(AppTheme.specs.padding)
    ) {
        HomeNavigationItemTransition(
            activeColor = selectedContentColor,
            inactiveColor = unselectedContentColor,
            selected = selected
        ) {
            HomeNavigationItemIcon(
                item = item,
                selected = selected
            )
            Text(
                stringResource(item.labelRes),
                maxLines = 1,
                overflow = TextOverflow.Visible,
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}
@Composable
internal fun HomeNavigationItemTransition(
    activeColor: Color,
    inactiveColor: Color,
    selected: Boolean,
    transitionSpec: AnimationSpec<Float> = TweenSpec(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    ),
    content: @Composable (animationProgress: Float) -> Unit
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = transitionSpec
    )

    val color = lerp(inactiveColor, activeColor, animationProgress)

    CompositionLocalProvider(
        LocalContentColor provides color.copy(alpha = 1f),
        LocalContentAlpha provides color.alpha,
    ) {
        content(animationProgress)
    }
}
