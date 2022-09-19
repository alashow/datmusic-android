/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose.previews

import androidx.compose.ui.tooling.preview.Preview

private const val Group = "Locale Previews"

@LocaleEnglishPreview
@LocaleFrenchPreview
@LocaleSpanishPreview
@LocaleGermanPreview
@LocaleTurkishPreview
@LocaleTurkmenPreview
@LocaleRussianPreview
annotation class LocalePreview

@Preview(
    name = "English",
    group = Group,
    locale = "en",
)
annotation class LocaleEnglishPreview

@Preview(
    name = "Spanish",
    group = Group,
    locale = "es",
)
annotation class LocaleSpanishPreview

@Preview(
    name = "German",
    group = Group,
    locale = "de",
)
annotation class LocaleGermanPreview

@Preview(
    name = "Turkmen",
    group = Group,
    locale = "tk",
)
annotation class LocaleTurkmenPreview

@Preview(
    name = "Turkish",
    group = Group,
    locale = "tr",
)
annotation class LocaleTurkishPreview

@Preview(
    name = "Russian",
    group = Group,
    locale = "ru",
)
annotation class LocaleRussianPreview

@Preview(
    name = "French",
    group = Group,
    locale = "fr",
)
annotation class LocaleFrenchPreview
