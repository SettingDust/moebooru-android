/*
 * Copyright (C) 2018 by onlymash <im@mash.im>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package im.mash.moebooru.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.*
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue

import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import im.mash.moebooru.R
import im.mash.moebooru.Settings
import im.mash.moebooru.glide.GetUrl
import im.mash.moebooru.glide.GlideApp
import im.mash.moebooru.ui.widget.FixedImageView
import im.mash.moebooru.utils.Key

@SuppressLint("RtlHardcoded")
class PostsFragment : ToolbarFragment(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private lateinit var drawer: Drawer
    private lateinit var drawerView: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToolbar: Toolbar

    private var metric: DisplayMetrics = DisplayMetrics()
    private var width: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        drawerView = inflater.inflate(R.layout.layout_drawer_posts, container, false)
        return inflater.inflate(R.layout.layout_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(this.requireContext(), R.color.colorPrimaryDark))

        toolbar.setTitle(R.string.posts)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setBackgroundColor(ContextCompat.getColor(this.requireContext(), R.color.post_toolbar))
        toolbar.setOnMenuItemClickListener(this)
        setGridItem()

        val activity = activity!!
        activity.windowManager.defaultDisplay.getMetrics(metric)
        width = metric.widthPixels

        drawer = DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withDrawerGravity(Gravity.RIGHT)
                .withDisplayBelowStatusBar(true)
                .withRootView(R.id.fragment_main)
                .withCustomView(drawerView)
                .withSavedInstance(savedInstanceState)
                .withDrawerWidthPx((width*0.75F).toInt())
                .withActionBarDrawerToggle(false)
                .buildForFragment()
        drawerLayout = drawer.drawerLayout

        drawerToolbar = drawerView.findViewById(R.id.toolbar_drawer_posts)
        drawerToolbar.setNavigationIcon(R.drawable.ic_action_close_24dp)
        drawerToolbar.inflateMenu(R.menu.menu_search)
        drawerToolbar.setOnMenuItemClickListener(this)
        drawerToolbar.setOnClickListener(this)

        val postsView: RecyclerView = view.findViewById(R.id.posts_list)
        when (Settings.gridModeString) {
            Key.GRID_MODE_GRID -> postsView.layoutManager = GridLayoutManager(this.requireContext(), 3, GridLayoutManager.VERTICAL, false)
            else -> postsView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
        val tv = TypedValue()
        var toolbarHeight = 0
        if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            toolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        postsView.adapter = PostAdapter(toolbarHeight, this.requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_grid -> {
                Settings.gridModeString = Key.GRID_MODE_GRID
                setGridItem()
            }
            R.id.action_staggered_grid -> {
                Settings.gridModeString = Key.GRID_MODE_STAGGERED_GRID
                setGridItem()
            }
            R.id.action_search_open -> {
                drawer.openDrawer()
            }
            R.id.action_search -> {
                drawer.closeDrawer()
            }
        }
        return true
    }

    private fun setGridItem() {
        when (Settings.gridModeString) {
            Key.GRID_MODE_GRID -> toolbar.menu.findItem(R.id.action_grid).setChecked(true)
            Key.GRID_MODE_STAGGERED_GRID -> toolbar.menu.findItem(R.id.action_staggered_grid).setChecked(true)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            drawerToolbar -> drawer.closeDrawer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        drawer.closeDrawer()
    }

    override fun onPause() {
        super.onPause()
        drawer.closeDrawer()
    }

    override fun onBackPressed(): Boolean {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return true
        }
        return super.onBackPressed()
    }

    private class PostAdapter(private val toolbarHeight: Int, private val context: Context) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

        companion object {
            private val items : List<String> = listOf(
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg",
                    "https://konachan.com/data/preview/e3/f1/e3f18776471c84b2fda5dc5552b07ce3.jpg",
                    "https://konachan.com/data/preview/34/69/346942d8a914f7821adf3560ee39f9ae.jpg",
                    "https://konachan.com/data/preview/fa/e6/fae61185bf4814bd06f06d0c4d5ae081.jpg"
            )

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_post_item, parent, false)
            return PostViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            holder.fixedImageView.setWidthAndHeightWeight(150, 106)
            if (position in 0..2) {
                val padding = context.resources.getDimension(R.dimen.item_padding)
                holder.itemView.setPadding(padding.toInt(), padding.toInt() + toolbarHeight, padding.toInt(), padding.toInt())
                Log.i(this.context.javaClass.simpleName, "toolbarHeight = $toolbarHeight")
            }
            GlideApp.with(holder.fixedImageView.context)
                    .load(GetUrl(items[position]).glideUrl)
                    .into(holder.fixedImageView)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val fixedImageView: FixedImageView = itemView.findViewById(R.id.post_item)
        }

    }
}