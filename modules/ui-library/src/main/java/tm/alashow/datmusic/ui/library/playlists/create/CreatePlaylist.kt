/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.ValidationError
import tm.alashow.base.util.asString
import tm.alashow.datmusic.ui.library.R
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedTextFieldColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreatePlaylist(
    viewModel: CreatePlaylistViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState(TextFieldValue())
    val nameError by viewModel.nameError.collectAsState(null)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.specs.padding),
    ) {
        Text(
            text = stringResource(R.string.library_createPlaylist_label),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )

        PlaylistNameInput(
            name = name,
            onSetName = viewModel::setPlaylistName,
            onCreatePlaylist = viewModel::createPlaylist,
            nameError = nameError,
        )

        TextRoundedButton(
            text = stringResource(R.string.library_createPlaylist),
            onClick = viewModel::createPlaylist,
        )
    }
}

@Composable
private fun PlaylistNameInput(
    name: TextFieldValue,
    onSetName: (TextFieldValue) -> Unit,
    onCreatePlaylist: () -> Unit,
    nameError: ValidationError?
) {
    TextField(
        value = name,
        onValueChange = onSetName,
        isError = nameError != null,
        textStyle = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Text),
        keyboardActions = KeyboardActions(onDone = { onCreatePlaylist() }),
        colors = outlinedTextFieldColors()
    )

    val context = LocalContext.current
    nameError?.let {
        Text(
            it.message.asString(context),
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption
        )
    }
}
