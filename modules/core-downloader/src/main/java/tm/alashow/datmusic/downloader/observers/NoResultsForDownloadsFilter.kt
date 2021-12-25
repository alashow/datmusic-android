/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import tm.alashow.datmusic.downloader.R
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.ValidationErrorException

data class NoResultsForDownloadsFilter(val params: ObserveDownloads.Params) :
    ValidationErrorException(
        ValidationError(
            when (params.hasQuery) {
                true -> UiMessage.Resource(
                    R.string.downloads_filter_noResults_forQuery,
                    listOf(params.query)
                )
                else -> UiMessage.Resource(R.string.downloads_filter_noResults)
            }
        )
    )
