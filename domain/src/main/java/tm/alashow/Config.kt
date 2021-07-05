/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow

import org.threeten.bp.Duration

object Config {
    const val API_BASE_URL = "https://api.datmusic.xyz/"

    val API_TIMEOUT = Duration.ofSeconds(40).toMillis()
}
