/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import java.util.Locale
import kotlinx.serialization.Serializable

private fun getLanguageCode() = Locale.getDefault().language

typealias SettingsLinks = List<SettingsLink>

const val LINK_NAME_SEPARATOR = "###"

@Serializable
class SettingsLink(
    val label: String,
    val link: String,
    val category: String? = null,
    // key=short language code, value=translated label/link/category
    private val labelTranslations: Map<String, String> = emptyMap(),
    private val linkTranslations: Map<String, String> = emptyMap(),
    private val categoryTranslations: Map<String, String> = emptyMap()
) {
    val localizedLabel get() = labelTranslations[getLanguageCode()] ?: label
    val localizedLink get() = linkTranslations[getLanguageCode()] ?: link
    val localizedCategory get() = categoryTranslations[getLanguageCode()] ?: category

    private fun getLinkParts() = localizedLink.split(LINK_NAME_SEPARATOR)
    fun getLinkName() = getLinkParts().first()
    fun getLinkUrl() = getLinkParts().let { parts ->
        if (parts.size > 1) parts[1] else parts[0]
    }
}
