/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.insets.statusBarsPadding
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.theme.AppTheme

@Preview
@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(MaterialTheme.colors.surface.copy(alpha = 0.95f))
            .fillMaxWidth()
    ) {
        var queryValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
        SearchTextField(
            value = queryValue,
            onValueChange = { value -> queryValue = value },
            hint = stringResource(R.string.search_hint),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = hint) },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.text.isNotEmpty(),
                enter = expandIn(Alignment.Center),
                exit = shrinkOut(Alignment.Center)
            ) {
                IconButton(
                    onClick = { onValueChange(TextFieldValue()) },
                ) {
                    Icon(
                        tint = MaterialTheme.colors.secondary,
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.generic_clear)
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = modifier
            .padding(AppTheme.specs.inputPaddings)
            .background(AppTheme.colors.onSurfaceInputBackground, MaterialTheme.shapes.small)
    )
}
