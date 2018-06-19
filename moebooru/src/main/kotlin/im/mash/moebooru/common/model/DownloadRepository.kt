package im.mash.moebooru.common.model

import im.mash.moebooru.common.data.local.MoeDatabase
import im.mash.moebooru.common.data.local.entity.PostDownload
import im.mash.moebooru.core.extensions.*
import im.mash.moebooru.core.scheduler.Outcome
import im.mash.moebooru.core.scheduler.Scheduler
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class DownloadRepository(private val database: MoeDatabase,
                         private val scheduler: Scheduler,
                         private val compositeDisposable: CompositeDisposable) : DownloadDataContract.Repository {

    companion object {
        private const val TAG = "DownloadRepository"
    }

    override val downloadPostsOutcome: PublishSubject<Outcome<MutableList<PostDownload>>>
            = PublishSubject.create<Outcome<MutableList<PostDownload>>>()

    override fun loadPosts() {
        downloadPostsOutcome.loading(true)
        database.postDownloadDao()
                .loadAll()
                .performOnBackOutOnMain(scheduler)
                .subscribe({ data ->
                    downloadPostsOutcome.success(data)
                }, { error -> handleError(error) })
                .addTo(compositeDisposable)
    }

    override fun addPost(post: PostDownload) {
        Completable.fromAction{
            database.postDownloadDao().save(post)
        }
                .performOnBack(scheduler)
                .subscribe({}, { error -> handleError(error)})
    }

    override fun deletePost(post: PostDownload) {
        Completable.fromAction{
            database.postDownloadDao().delete(post)
        }
                .performOnBack(scheduler)
                .subscribe({}, { error -> handleError(error)})
    }

    override fun deletePosts(posts: MutableList<PostDownload>) {
        Completable.fromAction{
            database.postDownloadDao().delete(posts)
        }
                .performOnBack(scheduler)
                .subscribe({}, { error -> handleError(error)})
    }

    override fun deleteAll() {
        Completable.fromAction{
            database.postDownloadDao().deleteAll()
        }
                .performOnBack(scheduler)
                .subscribe({}, { error -> handleError(error)})
    }

    override fun handleError(error: Throwable) {
        downloadPostsOutcome.failed(error)
    }

}