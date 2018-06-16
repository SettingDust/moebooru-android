package im.mash.moebooru.search.model

import im.mash.moebooru.common.data.local.entity.PostSearch
import im.mash.moebooru.core.scheduler.Outcome
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import okhttp3.HttpUrl

interface PostSearchDataContract {
    interface Repository {
        val postFetchOutcome: PublishSubject<Outcome<MutableList<PostSearch>>>
        fun isNotMore(): Boolean
        fun fetchPosts(httpUrl: HttpUrl)
        fun refreshPosts(httpUrl: HttpUrl)
        fun loadMorePosts(httpUrl: HttpUrl)
        fun addPosts(posts: MutableList<PostSearch>)
        fun deletePosts(site: String, tags: String)
        fun handleError(error: Throwable)
    }
    interface Local {
        fun getPosts(site: String, tags: String): Flowable<MutableList<PostSearch>>
        fun savePosts(site: String, tags: String, posts: MutableList<PostSearch>)
        fun addPosts(posts: MutableList<PostSearch>)
        fun deletePosts(site: String, tags: String)
    }
    interface Remote {
        fun getPosts(httpUrl: HttpUrl): Flowable<MutableList<PostSearch>>
    }
}