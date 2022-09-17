/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.ui.theme.Blue
import tm.alashow.ui.theme.Green
import tm.alashow.ui.theme.Orange
import tm.alashow.ui.theme.PreviewAppTheme
import tm.alashow.ui.theme.Primary
import tm.alashow.ui.theme.Theme

object AppButtonDefaults {
    val OutlinedButtonShape @Composable get() = Theme.shapes.small

    @Composable
    fun outlinedButtonColors(
        contentColor: Color = MaterialTheme.colorScheme.onSurface
    ) = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonDefaults.OutlinedButtonShape,
    colors: ButtonColors = AppButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) = OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(percent = 50),
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        onClick = onClick,
        shape = shape,
        enabled = enabled,
        modifier = modifier,
        content = content
    )
}

@Composable
fun TextRoundedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    enabled: Boolean = true,
) {
    AppButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text, style = textStyle)
    }
}

@CombinedPreview
@Composable
fun ButtonListPreview() = PreviewAppTheme {
    Column(
        Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextRoundedButton(onClick = {}, text = "Action")
        AppButton(onClick = {}, shape = RectangleShape) {
            Text("Action")
        }
        AppButton(onClick = {}, shape = MaterialTheme.shapes.small) {
            Text("Action", fontSize = 8.sp)
        }
        Spacer(Modifier.height(8.dp))
        AppButton(onClick = {}, backgroundColor = Blue) {
            Text("Action")
        }
        AppButton(onClick = {}, backgroundColor = Orange) {
            Text("Action")
        }
        AppButton(onClick = {}, backgroundColor = Green) {
            Text("Action")
        }
    }
}
