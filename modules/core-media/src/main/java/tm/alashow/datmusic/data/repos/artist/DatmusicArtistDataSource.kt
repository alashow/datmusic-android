/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.artist

import javax.inject.Inject
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.resultApiCall
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.DatmusicArtistParams.Companion.toQueryMap
import tm.alashow.datmusic.data.api.DatmusicEndpoints
import tm.alashow.datmusic.domain.models.ApiResponse
import tm.alashow.datmusic.domain.models.checkForErrors

class DatmusicArtistDataSource @Inject constructor(
    private val endpoints: DatmusicEndpoints,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(params: DatmusicArtistParams): Result<ApiResponse> {
        return resultApiCall(dispatchers.network) {
            endpoints.artist(params.id, params.toQueryMap())
                .checkForErrors()
        }
    }
}
