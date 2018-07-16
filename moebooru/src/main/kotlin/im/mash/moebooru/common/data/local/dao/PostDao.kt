package im.mash.moebooru.common.data.local.dao

import androidx.room.*
import im.mash.moebooru.common.data.local.entity.Post
import io.reactivex.Flowable

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): Flowable<MutableList<Post>>

    @Query("SELECT * FROM posts WHERE site = :site AND id = :id")
    fun getPost(site: String, id: Int): Flowable<Post>

    @Query("SELECT * FROM posts WHERE site = :site ORDER BY id DESC")
    fun getPosts(site: String): Flowable<MutableList<Post>>

    @Query("DELETE FROM posts WHERE site = :site")
    fun deletePosts(site: String)

    @Query("DELETE FROM posts WHERE site = :site AND id NOT IN (SELECT id FROM posts WHERE site = :site ORDER BY id DESC LIMIT :limit)")
    fun deletePosts(site: String, limit: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPosts(posts: MutableList<Post>)

    @Delete
    fun delete(post: Post)
}