/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.search

import javax.inject.Inject
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.resultApiCall
import tm.alashow.datmusic.data.api.Endpoints
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.Companion.toQueryMap
import tm.alashow.domain.models.ApiResponse
import tm.alashow.domain.models.checkForErrors

class DatmusicSearchDataSource @Inject constructor(
    private val endpoints: Endpoints,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(params: DatmusicSearchParams): Result<ApiResponse> {
        return resultApiCall(dispatchers.network) {
            endpoints.multisearch(params.toQueryMap(), *params.types.toTypedArray())
                .checkForErrors()
        }
    }
}
