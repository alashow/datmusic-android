/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow

import org.threeten.bp.Duration

object Config {
    const val BASE_HOST = "datmusic.xyz"
    const val BASE_URL = "https://$BASE_HOST/"
    const val API_BASE_URL = "https://demo-api.$BASE_HOST/"

    const val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=tm.alashow.datmusic"

    val API_TIMEOUT = Duration.ofSeconds(40).toMillis()
    val DOWNLOADER_TIMEOUT = Duration.ofMinutes(3).toMillis()
    val PLAYER_TIMEOUT = Duration.ofMinutes(2).toMillis()
    val PLAYER_TIMEOUT_CONNECT = Duration.ofSeconds(30).toMillis()
}
