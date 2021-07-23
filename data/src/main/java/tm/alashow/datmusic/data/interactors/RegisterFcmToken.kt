/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors

import javax.inject.Inject
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.data.resultApiCall
import tm.alashow.datmusic.data.api.DatmusicEndpoints

class RegisterFcmToken @Inject constructor(
    private val api: DatmusicEndpoints,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<RegisterFcmToken.Params, String>() {

    data class Params(val token: String)

    override suspend fun doWork(params: Params) = resultApiCall(dispatchers.network) {
        api.registerFcmToken(params.token)
    }.map { it.data.message }.getOrThrow()
}
