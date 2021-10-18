/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight

// TODO: not sure if this is the best way to define styles
@Composable
fun topAppBarTitleStyle() = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)

@Composable
fun topAppBarTitleStyleSmall() = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)

@Composable
fun textShadow(color: Color = Color.Black, offset: Offset = Offset(0f, 1f), radius: Float = 0.4f) = Shadow(color, offset, radius)

@Composable
fun borderlessTextFieldColors(
    cursorColor: Color = MaterialTheme.colors.secondary,
) = outlinedTextFieldColors(cursorColor, Color.Transparent, Color.Transparent)

@Composable
fun outlinedTextFieldColors(
    cursorColor: Color = MaterialTheme.colors.secondary,
    focusedBorderColor: Color = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium),
    unfocusedBorderColor: Color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    cursorColor = cursorColor,
)

@Composable
fun outlinedButtonColors(contentColor: Color = MaterialTheme.colors.onSurface) =
    ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
