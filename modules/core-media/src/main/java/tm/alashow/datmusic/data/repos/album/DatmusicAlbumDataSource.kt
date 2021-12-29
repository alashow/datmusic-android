/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.album

import javax.inject.Inject
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.resultApiCall
import tm.alashow.datmusic.data.DatmusicAlbumParams
import tm.alashow.datmusic.data.DatmusicAlbumParams.Companion.toQueryMap
import tm.alashow.datmusic.data.api.DatmusicEndpoints
import tm.alashow.datmusic.domain.models.ApiResponse
import tm.alashow.datmusic.domain.models.checkForErrors

class DatmusicAlbumDataSource @Inject constructor(
    private val endpoints: DatmusicEndpoints,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(params: DatmusicAlbumParams): Result<ApiResponse> {
        return resultApiCall(dispatchers.network) {
            endpoints.album(params.id, params.toQueryMap())
                .checkForErrors()
        }
    }
}
