/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import tm.alashow.ui.material.ContentAlpha

// TODO: not sure if this is the best way to define styles
@Composable
fun topAppBarTitleStyle() = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)

@Composable
fun topAppBarTitleStyleSmall() = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

@Composable
fun textShadow(color: Color = Color.Black, offset: Offset = Offset(0f, 1f), radius: Float = 0.4f) = Shadow(color, offset, radius)

@Composable
fun borderlessTextFieldColors(
    cursorColor: Color = MaterialTheme.colorScheme.secondary,
) = outlinedTextFieldColors(cursorColor, Color.Transparent, Color.Transparent)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun outlinedTextFieldColors(
    cursorColor: Color = MaterialTheme.colorScheme.secondary,
    focusedBorderColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = ContentAlpha.medium),
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
) = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    cursorColor = cursorColor,
)
