/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.components.ChipsRow
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.topAppBarTitleStyle
import tm.alashow.datmusic.ui.theme.translucentSurface

@Composable
fun Search() {
    Search(
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun Search(
    viewModel: SearchViewModel,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = SearchViewState.Empty)

    Search(viewModel, viewState) { action ->
        viewModel.submitAction(action)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun Search(
    viewModel: SearchViewModel,
    state: SearchViewState,
    actioner: (SearchAction) -> Unit
) {
    Scaffold(
        topBar = {
            SearchAppBar(onSearchQueryChange = { actioner(SearchAction.Search(it)) })
        }
    ) { padding ->
        Column {
            // ArtistList(viewModel, padding)
            AudioList(viewModel, padding)
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalAnimationApi::class)
fun SearchAppBar(
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
) {
    Box(
        modifier = modifier
            .translucentSurface()
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        val keyboardVisible = LocalWindowInsets.current.ime.isVisible

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
            AnimatedVisibility(visible = !keyboardVisible) {
                Text(
                    text = stringResource(R.string.search_title),
                    style = topAppBarTitleStyle(),
                    modifier = Modifier.padding(start = AppTheme.specs.padding, top = AppTheme.specs.padding),
                )
            }

            var queryValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

            SearchTextField(
                value = queryValue,
                onValueChange = { value ->
                    queryValue = value
                    onSearchQueryChange(value.text)
                },
                hint = if (!keyboardVisible) stringResource(R.string.search_hint) else stringResource(R.string.search_hint_query),
                modifier = Modifier.fillMaxWidth(),
            )

            val (selected, setSelected) = remember { mutableStateOf("") }

            AnimatedVisibility(visible = keyboardVisible) {
                ChipsRow(
                    listOf("Songs", "Artists", "Albums"), selected,
                    onItemSelect = { selected, item ->
                        if (selected) setSelected(item)
                        else setSelected("")
                    }
                )
            }
        }
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
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colors.secondary
        ),
        modifier = modifier
            .padding(horizontal = AppTheme.specs.padding)
            .background(AppTheme.colors.onSurfaceInputBackground, MaterialTheme.shapes.small)
    )
}
