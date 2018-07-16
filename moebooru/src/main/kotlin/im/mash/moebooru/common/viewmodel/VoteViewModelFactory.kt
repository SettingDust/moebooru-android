package im.mash.moebooru.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import im.mash.moebooru.common.model.VoteDataContract
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
class VoteViewModelFactory(private val repository: VoteDataContract.Repository,
                           private val compositeDisposable: CompositeDisposable) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return VoteViewModel(repository, compositeDisposable) as T
    }
}