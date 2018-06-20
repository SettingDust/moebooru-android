package im.mash.moebooru.main.model


import im.mash.moebooru.common.data.local.MoeDatabase
import im.mash.moebooru.common.data.local.entity.Post
import im.mash.moebooru.core.extensions.performOnBack
import im.mash.moebooru.core.scheduler.Scheduler
import im.mash.moebooru.util.logi
import io.reactivex.Completable
import io.reactivex.Flowable

class PostLocalData(private val database: MoeDatabase,
                    private val scheduler: Scheduler)
    : PostDataContract.Local {

    companion object {
        private const val TAG = "PostLocalData"
    }

    override fun getPosts(site: String): Flowable<MutableList<Post>> {
        return database.postDao().getPosts(site)
    }

    override fun addPosts(posts: MutableList<Post>) {
        Completable.fromAction{
            database.postDao().insertPosts(posts)
        }
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun deletePosts(site: String) {
        database.postDao().deletePosts(site)
    }
}