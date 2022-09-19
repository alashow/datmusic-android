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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.IntentUtils
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.ui.components.AppOutlinedButton
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Theme

@Composable
internal fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Theme.typography.h6,
        color = MaterialTheme.colorScheme.secondary,
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
    analytics: Analytics = LocalAnalytics.current
) {
    SettingsItem(label, verticalAlignment = Alignment.Top) {
        val context = LocalContext.current
        ClickableText(
            text = buildAnnotatedString { append(text) },
            style = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onBackground,
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
            style = MaterialTheme.typography.titleMedium,
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
    onClick: () -> Unit,
) {
    AppOutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier,
    ) {
        if (isLoading)
            ProgressIndicatorSmall(Modifier.padding(end = AppTheme.specs.paddingSmall))
        Text(text, maxLines = 1)
    }
}
