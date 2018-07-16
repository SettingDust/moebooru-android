package im.mash.moebooru.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import im.mash.moebooru.main.model.PoolDataContract
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
class PoolViewModelFactory(private val repo: PoolDataContract.Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PoolViewModel(repo) as T
    }
}