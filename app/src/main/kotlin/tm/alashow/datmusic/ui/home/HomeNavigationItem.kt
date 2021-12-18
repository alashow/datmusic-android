/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import tm.alashow.navigation.screens.RootScreen

internal sealed class HomeNavigationItem(
    val screen: RootScreen,
    @StringRes val labelRes: Int,
    @StringRes val contentDescriptionRes: Int,
) {
    class ResourceIcon(
        screen: RootScreen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        @DrawableRes val iconResId: Int,
        @DrawableRes val selectedIconRes: Int? = null,
    ) : HomeNavigationItem(screen, labelResId, contentDescriptionResId)

    class ImageVectorIcon(
        screen: RootScreen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        val iconImageVector: ImageVector,
        val selectedImageVector: ImageVector? = null,
    ) : HomeNavigationItem(screen, labelResId, contentDescriptionResId)
}
