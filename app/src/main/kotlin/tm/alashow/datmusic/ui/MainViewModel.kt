/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.ui.base.vm.RxAwareViewModel
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.datmusic.data.api.Endpoints
import tm.alashow.datmusic.data.api.HttpBinResponse

data class MainViewState(
    val response: HttpBinResponse = HttpBinResponse(),
    val error: Throwable = Throwable()
) {
    companion object {
        val Empty = MainViewState()
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val handle: SavedStateHandle,
    val api: Endpoints,
    val schedulers: AppRxSchedulers
) : RxAwareViewModel() {

    private val responseState = MutableStateFlow(HttpBinResponse())
    private val responseError = MutableStateFlow(Throwable())

    val state = combine(responseState, responseError) { response, error ->
        MainViewState(response, error)
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = api.getHttpBin()
                    responseState.value = response
                    Timber.d(response.toString())
                } catch (e: Exception) {
                    responseError.value = e
                    Timber.e(e)
                }
            }
        }
    }
}
