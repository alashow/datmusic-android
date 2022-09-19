/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import tm.alashow.base.util.Analytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.ui.theme.Theme
import tm.alashow.ui.theme.borderlessTextFieldColors

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    withIcon: Boolean = false,
    autoFocus: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    ),
    hint: String = stringResource(R.string.search_hint_query),
    maxLength: Int = 200,
    analyticsPrefix: String = "search",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Search,
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions = KeyboardActions(onSearch = { onSearch() }),
    analytics: Analytics = LocalAnalytics.current,
) {
    val focusRequester = remember { FocusRequester() }
    DisposableEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
        onDispose { }
    }

    OutlinedTextField(
        value = value,
        leadingIcon = if (withIcon) {
            { SearchTextFieldIcon() }
        } else null,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        placeholder = { Text(text = hint, style = textStyle) },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = expandIn(expandFrom = Alignment.Center),
                exit = shrinkOut(shrinkTowards = Alignment.Center)
            ) {
                IconButton(
                    onClick = {
                        onValueChange("")
                        analytics.click("$analyticsPrefix.clear")
                    },
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.secondary,
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.generic_clear)
                    )
                }
            }
        },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textStyle,
        colors = borderlessTextFieldColors(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Theme.specs.padding)
            .background(Theme.colors.elevatedSurface, MaterialTheme.shapes.small)
            .focusRequester(focusRequester)
    )
}

@Composable
fun SearchTextFieldIcon() {
    Icon(
        tint = MaterialTheme.colorScheme.onBackground,
        imageVector = Icons.Default.Search,
        contentDescription = null
    )
}
