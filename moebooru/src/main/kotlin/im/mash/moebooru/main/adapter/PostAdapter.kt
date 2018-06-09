package im.mash.moebooru.main.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.model.GlideUrl
import im.mash.moebooru.R
import im.mash.moebooru.Settings
import im.mash.moebooru.common.data.local.entity.Post
import im.mash.moebooru.core.widget.FixedImageView
import im.mash.moebooru.glide.GlideApp
import im.mash.moebooru.util.*

class PostAdapter(private val context: Context, private var gridMode: String) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    companion object {
        private const val TAG = "PostAdapter"
    }

    private val header = context.glideHeader
    private var posts = mutableListOf<Post>()
    private var spanCount = context.screenWidth/context.resources.getDimension(R.dimen.item_width).toInt()
    private val padding = context.resources.getDimension(R.dimen.item_padding).toInt()

    fun updateData(posts: MutableList<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    fun setGridMode(gridMode: String) {
        this.gridMode = gridMode
    }

    fun addData(posts: MutableList<Post>) {
        val countBefore = itemCount
        this.posts = posts
        notifyItemRangeInserted(countBefore, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_post_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (position in 0 until spanCount) {
            holder.itemView.setPadding(padding, padding + context.toolbarHeight + statusBarHeight, padding, padding)
        } else {
            holder.itemView.setPadding(padding, padding, padding, padding)
        }
        val placeHolderId = when (posts[position].rating) {
            "q" -> R.drawable.background_rating_q
            "e" -> R.drawable.background_rating_e
            else -> R.drawable.background_rating_s
        }
        when (gridMode) {
            Settings.GRID_MODE_GRID -> {
                holder.fixedImageView.setWidthAndHeightWeight(1, 1)
                GlideApp.with(context)
                        .load(GlideUrl(posts[position].preview_url, header))
                        .centerCrop()
                        .placeholder(placeHolderId)
                        .into(holder.fixedImageView)
            }
            else -> {
                holder.fixedImageView.setWidthAndHeightWeight(posts[position].width, posts[position].height)
                GlideApp.with(context)
                        .load(GlideUrl(posts[position].preview_url, header))
                        .fitCenter()
                        .placeholder(placeHolderId)
                        .into(holder.fixedImageView)
            }
        }

    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fixedImageView: FixedImageView = itemView.findViewById(R.id.post_item)
    }
}