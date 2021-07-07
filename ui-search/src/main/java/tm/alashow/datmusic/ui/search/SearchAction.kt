/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.domain.models.errors.ApiCaptchaError

internal sealed class SearchAction {
    data class QueryChange(val query: String = "") : SearchAction()
    object Search : SearchAction()
    data class SelectBackendType(val selected: Boolean, val backendType: DatmusicSearchParams.BackendType) : SearchAction()

    data class AddError(val error: Throwable) : SearchAction()
    object ClearError : SearchAction()
    data class SolveCaptcha(val captchaError: ApiCaptchaError, val key: String) : SearchAction()
}
