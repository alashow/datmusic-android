/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.datmusic.ui.library.R
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedTextFieldColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreatePlaylist(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val (name, setName) = remember { mutableStateOf(TextFieldValue()) }
    Column(
        modifier = Modifier.fillMaxHeight(1f).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingLarge, Alignment.CenterVertically)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = setName,
            placeholder = {
                Text(stringResource(R.string.library_createPlaylist_placeholder))
            },
            colors = outlinedTextFieldColors()
        )

        TextRoundedButton(
            onClick = { viewModel.createPlaylist(name.text) },
            text = stringResource(R.string.library_createPlaylist)
        )
    }
}
