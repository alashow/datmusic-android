/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tm.alashow.datmusic.data.repos.CaptchaSolution
import tm.alashow.datmusic.data.repos.search.BackendTypes
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.domain.models.errors.ApiCaptchaError

@Parcelize
data class SearchFilter(
    val backends: BackendTypes = DefaultBackends
) : Parcelable {

    companion object {
        val DefaultBackends: BackendTypes = DatmusicSearchParams.BackendType.values().toSet()
    }
}

data class SearchViewState(
    val searchFilter: SearchFilter = SearchFilter(),
    val error: Throwable? = null,
    val captchaError: ApiCaptchaError? = null,
) {
    companion object {
        val Empty = SearchViewState()
    }
}

@Parcelize
data class SearchTrigger(val query: String = "", val captchaSolution: CaptchaSolution? = null) : Parcelable
