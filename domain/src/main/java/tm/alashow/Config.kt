/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow

import org.threeten.bp.Duration

object Config {
    const val BASE_HOST = "datmusic.xyz"
    const val BASE_URL = "https://$BASE_HOST/"
    const val API_BASE_URL = "https://api.$BASE_HOST/"

    val API_TIMEOUT = Duration.ofSeconds(40).toMillis()
    val DOWNLOADER_TIMEOUT = Duration.ofMinutes(3).toMillis()
}
