package im.mash.moebooru.main.model

import im.mash.moebooru.common.data.local.entity.Tag
import im.mash.moebooru.core.extensions.*
import im.mash.moebooru.core.scheduler.Outcome
import im.mash.moebooru.core.scheduler.Scheduler
import im.mash.moebooru.util.logi
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class TagRepository(private val local: TagDataContract.Local,
                    private val scheduler: Scheduler,
                    private val compositeDisposable: CompositeDisposable) : TagDataContract.Repository {

    companion object {
        private const val TAG = "TagRepository"
    }

    override val tagFetchOutcome: PublishSubject<Outcome<MutableList<Tag>>>
        get() = PublishSubject.create<Outcome<MutableList<Tag>>>()

    override fun getTags(site: String) {
        tagFetchOutcome.loading(true)
        local.getTags(site)
                .performOnBackOutOnMain(scheduler)
                .subscribe({ tags ->
                    logi(TAG, "getTags success")
                    tagFetchOutcome.success(tags)
                }, {  error -> handleError(error)})
                .addTo(compositeDisposable)
    }

    override fun saveTag(tag: Tag) {
        local.saveTag(tag)
    }

    override fun deleteTag(tag: Tag) {
        local.deleteTag(tag)
    }

    override fun handleError(error: Throwable) {
        tagFetchOutcome.failed(error)
    }
}