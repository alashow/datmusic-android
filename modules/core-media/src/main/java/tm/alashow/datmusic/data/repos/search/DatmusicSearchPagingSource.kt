/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dropbox.android.external.store4.get
import tm.alashow.datmusic.data.DATMUSIC_FIRST_PAGE_INDEX
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.interactors.search.SearchDatmusic
import tm.alashow.domain.models.PaginatedEntity

/**
 * Uses [DatmusicSearchStore] to paginate in-memory items that were already fetched via [SearchDatmusic].
 * Not being used anymore, keeping just for reference.
 */
class DatmusicSearchPagingSource<T : PaginatedEntity>(
    private val datmusicSearchStore: DatmusicSearchStore<T>,
    private val searchParams: DatmusicSearchParams
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: DATMUSIC_FIRST_PAGE_INDEX
        return try {
            val items = datmusicSearchStore.get(searchParams.copy(page = page))

            LoadResult.Page(
                data = items,
                prevKey = if (page == DATMUSIC_FIRST_PAGE_INDEX) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
