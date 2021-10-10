/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.statusBarsPadding
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.common.compose.getNavArgument
import tm.alashow.datmusic.data.repos.playlist.PlaylistArtworkUtils.getPlaylistArtworkImageFile
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.library.playlists.PlaylistNameInput
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY
import tm.alashow.ui.KeyboardSpacer
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.extensions.centerHorizontally
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditPlaylist(
    viewModel: EditPlaylistViewModel = hiltViewModel(),
) {
    val playlistId = getNavArgument(PLAYLIST_ID_KEY) as PlaylistId
    val name by viewModel.name.collectAsState(TextFieldValue())
    val nameError by viewModel.nameError.collectAsState(null)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.specs.padding)
            .statusBarsPadding(),
    ) {
        val (label, image, input, button, spacer, deleteButton) = createRefs()

        Text(
            text = stringResource(R.string.playlist_edit_label),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier.constrainAs(label) {
                top.linkTo(parent.top)
                centerHorizontally()
            }
        )

        val context = LocalContext.current
        val imagePainter = rememberImagePainter(playlistId.getPlaylistArtworkImageFile(context), builder = ImageLoading.defaultConfig)

        CoverImage(
            painter = imagePainter,
            modifier = Modifier
                .constrainAs(image) {
                    height = Dimension.fillToConstraints
                    top.linkTo(label.bottom)
                    bottom.linkTo(input.top)
                    centerHorizontally()
                }
                .padding(AppTheme.specs.paddingLarge)
        ) { imageMod ->
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = imageMod
            )
        }

        PlaylistNameInput(
            name = name,
            onSetName = viewModel::setPlaylistName,
            onDone = viewModel::save,
            nameError = nameError,
            modifier = Modifier.constrainAs(input) {
                top.linkTo(parent.top)
                bottom.linkTo(spacer.top)
                centerHorizontally()
            }
        )

        val padding = AppTheme.specs.paddingLarge
        TextRoundedButton(
            text = stringResource(R.string.playlist_edit_done),
            onClick = viewModel::save,
            modifier = Modifier.constrainAs(button) {
                top.linkTo(input.bottom, padding)
                centerHorizontally()
            }
        )

        TextButton(
            onClick = viewModel::deletePlaylistItem,
            colors = textButtonColors(contentColor = MaterialTheme.colors.error),
            modifier = Modifier.constrainAs(deleteButton) {
                bottom.linkTo(parent.bottom)
                centerHorizontally()
            }
        ) {
            Text(stringResource(R.string.playlist_edit_delete))
        }

        KeyboardSpacer(
            Modifier.constrainAs(spacer) {
                bottom.linkTo(parent.bottom)
            }
        )
    }
}
