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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.base.util.click
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.borderlessTextFieldColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    withIcon: Boolean = false,
    autoFocus: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.subtitle2.copy(
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
    analytics: FirebaseAnalytics = LocalAnalytics.current,
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
                        tint = MaterialTheme.colors.secondary,
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
            .heightIn(min = 16.dp)
            .background(AppTheme.colors.onSurfaceInputBackground, MaterialTheme.shapes.small)
            .focusRequester(focusRequester)
    )
}

@Composable
fun SearchTextFieldIcon() {
    Icon(
        tint = MaterialTheme.colors.onBackground,
        imageVector = Icons.Default.Search,
        contentDescription = null
    )
}
