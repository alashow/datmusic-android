/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import tm.alashow.base.util.asString
import tm.alashow.i18n.ValidationError
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedTextFieldColors

@Composable
internal fun PlaylistNameInput(
    modifier: Modifier = Modifier,
    name: TextFieldValue = TextFieldValue(),
    onSetName: (TextFieldValue) -> Unit = {},
    onDone: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Done,
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions = KeyboardActions(onDone = { onDone() }),
    nameError: ValidationError? = null,
) {
    Column(modifier) {
        TextField(
            value = name,
            onValueChange = onSetName,
            isError = nameError != null,
            textStyle = MaterialTheme.typography.h4.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = outlinedTextFieldColors(),
        )

        val context = LocalContext.current
        nameError?.let {
            Text(
                it.message.asString(context),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = AppTheme.specs.paddingSmall)
            )
        }
    }
}
