/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.ui.Zoomable
import tm.alashow.ui.colorFilterDynamicProperty
import tm.alashow.ui.theme.AppTheme

@Composable
fun EmptyErrorBox(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_empty_title),
    message: String = stringResource(R.string.error_empty),
    retryLabel: String = stringResource(R.string.error_retry),
    retryVisible: Boolean = true,
    onRetryClick: () -> Unit = {},
) {
    ErrorBox(
        title = title,
        message = message,
        retryLabel = retryLabel,
        retryVisible = retryVisible,
        onRetryClick = onRetryClick,
        modifier = modifier
    )
}

@CombinedPreview
@Composable
fun ErrorBox(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_title),
    message: String = stringResource(R.string.error_unknown),
    retryLabel: String = stringResource(R.string.error_retry),
    retryVisible: Boolean = true,
    onRetryClick: () -> Unit = {}
) {
    val loadingYOffset = 130.dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .offset(y = loadingYOffset / 3f)
    ) {
        val wavesComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.waves))

        Zoomable {
            LottieAnimation(
                wavesComposition,
                speed = 0.5f,
                iterations = LottieConstants.IterateForever,
                dynamicProperties = colorFilterDynamicProperty(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                modifier = Modifier.offset(y = -loadingYOffset)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = AppTheme.specs.paddingLarge)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(message)
            if (retryVisible)
                TextRoundedButton(
                    onClick = onRetryClick,
                    text = retryLabel,
                    modifier = Modifier.padding(top = AppTheme.specs.padding)
                )
        }
    }
}
