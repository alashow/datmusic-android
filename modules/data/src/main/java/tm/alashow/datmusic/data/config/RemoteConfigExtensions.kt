/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.config

import kotlinx.serialization.builtins.ListSerializer
import tm.alashow.data.RemoteConfig
import tm.alashow.datmusic.domain.entities.SettingsLink

const val REMOTE_CONFIG_SETTINGS_LINKS_KEY = "settings_links"

fun RemoteConfig.getSettingsLinks() = get(REMOTE_CONFIG_SETTINGS_LINKS_KEY, ListSerializer(SettingsLink.serializer()), emptyList())
