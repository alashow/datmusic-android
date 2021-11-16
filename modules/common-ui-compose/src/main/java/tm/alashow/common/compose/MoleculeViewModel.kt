package tm.alashow.common.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import app.cash.molecule.AndroidUiDispatcher.Companion.Main

open class MoleculeViewModel : ViewModel() {

    val scope = CoroutineScope(viewModelScope.coroutineContext + Main)
}
