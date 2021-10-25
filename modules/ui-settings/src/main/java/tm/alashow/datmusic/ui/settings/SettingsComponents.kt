/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ButtonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.analytics.FirebaseAnalytics
import tm.alashow.base.util.IntentUtils
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedButtonColors

@Composable
internal fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text, style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.secondary,
        modifier = modifier.padding(AppTheme.specs.inputPaddings)
    )
}

@Composable
internal fun SettingsLinkItem(
    @StringRes labelRes: Int,
    @StringRes textRes: Int,
    @StringRes linkRes: Int,
) {
    SettingsLinkItem(stringResource(labelRes), stringResource(textRes), stringResource(linkRes))
}

@Composable
internal fun SettingsLinkItem(
    label: String,
    text: String,
    link: String,
    analytics: FirebaseAnalytics = LocalAnalytics.current
) {
    SettingsItem(label, verticalAlignment = Alignment.Top) {
        val context = LocalContext.current
        ClickableText(
            text = buildAnnotatedString { append(text) },
            style = TextStyle.Default.copy(
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.End
            ),
            onClick = {
                analytics.event("settings.linkClick", mapOf("link" to link))
                IntentUtils.openUrl(context, link)
            }
        )
    }
}

@Composable
internal fun SettingsItem(
    label: String,
    modifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier,
    labelWeight: Float = 1f,
    contentWeight: Float = 1f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = verticalAlignment,
        modifier = modifier
            .padding(horizontal = AppTheme.specs.padding)
            .fillMaxWidth()
    ) {
        Text(
            label,
            style = MaterialTheme.typography.subtitle1,
            modifier = labelModifier
                .padding(end = AppTheme.specs.paddingTiny)
                .weight(labelWeight)
        )
        Box(
            modifier = Modifier.weight(contentWeight, false),
            contentAlignment = Alignment.CenterEnd
        ) { content() }
    }
}

@Composable
internal fun SettingsLoadingButton(
    isLoading: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = outlinedButtonColors(),
    onClick: Callback,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = colors,
        modifier = modifier,
    ) {
        if (isLoading)
            ProgressIndicatorSmall(Modifier.padding(end = AppTheme.specs.paddingSmall))
        Text(text, maxLines = 1)
    }
}
