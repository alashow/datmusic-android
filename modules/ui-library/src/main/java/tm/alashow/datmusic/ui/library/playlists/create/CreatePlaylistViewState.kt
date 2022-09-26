/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import javax.annotation.concurrent.Immutable
import tm.alashow.i18n.ValidationError

@Immutable
internal data class CreatePlaylistViewState(
    val name: String = "",
    val nameError: ValidationError? = null,
) {
    companion object {
        val Empty = CreatePlaylistViewState()
    }
}
