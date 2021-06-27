/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.domain.models.ApiResponse

interface Endpoints {

    @GET("/multisearch")
    @JvmSuppressWildcards
    suspend fun multisearch(@QueryMap params: Map<String, Any>, @Query("types[]") vararg types: DatmusicSearchParams.BackendType): ApiResponse

    @GET("/search/artists")
    @JvmSuppressWildcards
    suspend fun searchArtists(@QueryMap params: Map<String, Any>, @Query("types[]") vararg types: DatmusicSearchParams.BackendType): ApiResponse

    @GET("/search/albums")
    @JvmSuppressWildcards
    suspend fun searchAlbums(@QueryMap params: Map<String, Any>, @Query("types[]") vararg types: DatmusicSearchParams.BackendType): ApiResponse

    @GET("/bytes/{searchKey}/{audioId}")
    suspend fun bytes(@Path("searchKey") searchKey: String, @Path("audioId") audioId: String): ApiResponse
}
