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
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import im.mash.moebooru.App.Companion.app
import im.mash.moebooru.R
import im.mash.moebooru.ui.adapter.PostsAdapter
import im.mash.moebooru.utils.Key
import im.mash.moebooru.utils.TableType

class SearchFragment : BasePostsFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG = this.javaClass.simpleName

    private val searchActivity by lazy { this.activity as SearchActivity }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        toolbar = inflater.inflate(R.layout.layout_toolbar, null) as Toolbar
        return inflater.inflate(R.layout.layout_posts_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = this.requireContext()
        type = TableType.SEARCH
        toolbar.setTitle(R.string.posts)
        toolbar.inflateMenu(R.menu.menu_main_search)
        searchActivity.setActionBar(toolbar)
        setToolbarGridOption()
        setInsetsListener(toolbar)
        val bundle = arguments
        if (bundle != null) {
            tags = bundle.getString(Key.TAGS_SEARCH)
            if (tags != null) toolbar.subtitle = tags
        }
        //SwipeRefreshLayout
        refreshLayout = view.findViewById(R.id.refresh)
        setSwipeRefreshLayout(refreshLayout, searchActivity.toolbarHeight)
        refreshLayout.setOnRefreshListener(this)

        //计算列数
        spanCount = searchActivity.widthScreen/searchActivity.resources.getDimension(R.dimen.item_width).toInt()
        app.settings.spanCountInt = spanCount

        //item 边距
        itemPadding = searchActivity.resources.getDimension(R.dimen.item_padding).toInt()

        //init Adapter
        postsAdapter = PostsAdapter(this.requireContext(), itemPadding,
                searchActivity.toolbarHeight + app.settings.statusBarHeightInt,null)

        //init RecyclerView
        initPostsView(view)

        //监听设置变化
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        sp.registerOnSharedPreferenceChangeListener(this)

        loadData()
    }

//    override fun onMenuItemClick(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.action_grid -> app.settings.gridModeString = Key.GRID_MODE_GRID
//            R.id.action_staggered_grid -> app.settings.gridModeString = Key.GRID_MODE_STAGGERED_GRID
//        }
//        return true
//    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Key.GRID_MODE -> {
                setToolbarGridOption()
                reSetupGridMode()
            }
            Key.ACTIVE_PROFILE -> {
                app.settings.isNotMoreData = false
                postsAdapter.updateData(null)
                loadData()
            }
        }
    }
}