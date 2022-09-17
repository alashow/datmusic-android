/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import timber.log.Timber
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.LogArgs

internal object PreviewAnalytics : Analytics {
    override fun logEvent(event: String, args: LogArgs) {
        Timber.d("Analytics#logEvent: $event, args: $args")
    }
}
