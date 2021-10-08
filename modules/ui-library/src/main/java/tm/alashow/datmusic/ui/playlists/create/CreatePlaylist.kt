/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playlists.create

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.datmusic.ui.library.R
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedTextFieldColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreatePlaylist(
    viewModel: CreatePlaylistViewModel = hiltViewModel()
) {
    val (name, setName) = remember { mutableStateOf(TextFieldValue()) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.specs.padding),
    ) {
        val textStyle = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center)
        Text(
            text = stringResource(R.string.library_createPlaylist_placeholder),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )

        TextField(
            value = name,
            onValueChange = setName,
            textStyle = textStyle,
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onDone = { viewModel.createPlaylist(name.text) }),
            colors = outlinedTextFieldColors()
        )

        TextRoundedButton(
            onClick = { viewModel.createPlaylist(name.text) },
            text = stringResource(R.string.library_createPlaylist)
        )
    }
}
