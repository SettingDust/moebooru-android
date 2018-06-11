package im.mash.moebooru.detail

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.Log
import im.mash.moebooru.App.Companion.app
import im.mash.moebooru.R
import im.mash.moebooru.common.MoeDH
import im.mash.moebooru.common.data.local.entity.Post
import im.mash.moebooru.common.data.local.entity.PostSearch
import im.mash.moebooru.core.application.SlidingActivity
import im.mash.moebooru.core.network.Outcome
import im.mash.moebooru.core.widget.VerticalViewPager
import im.mash.moebooru.detail.adapter.DetailAdapter
import im.mash.moebooru.detail.fragment.InfoFragment
import im.mash.moebooru.detail.fragment.PagerFragment
import im.mash.moebooru.detail.fragment.TagFragment
import im.mash.moebooru.detail.viewmodel.DetailViewModel
import im.mash.moebooru.detail.viewmodel.DetailViewModelFactory
import im.mash.moebooru.detail.viewmodel.PositionViewModel
import im.mash.moebooru.detail.viewmodel.PositionViewModelFactory
import im.mash.moebooru.helper.getViewModel
import javax.inject.Inject

class DetailActivity : SlidingActivity() {

    companion object {
        private const val TAG = "DetailActivity"
    }

    internal var paddingBottom = 0
    internal var paddingTop = 0

    private lateinit var detailPager: VerticalViewPager
    private lateinit var detailAdapter: DetailAdapter

    private val component by lazy { MoeDH.detailComponent() }

    @Inject
    lateinit var detailViewModelFactory: DetailViewModelFactory
    private val detailViewModel: DetailViewModel by lazy { this.getViewModel<DetailViewModel>(detailViewModelFactory) }

    @Inject
    lateinit var positionViewModelFactory: PositionViewModelFactory
    internal val positionViewModel: PositionViewModel by lazy { this.getViewModel<PositionViewModel>(positionViewModelFactory) }

    internal var posts: MutableList<Post> = mutableListOf()
    internal var postsSearch: MutableList<PostSearch> = mutableListOf()

    internal var type = "post"
    internal var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        component.inject(this)
        initView()
        val tags = intent.extras["tags"].toString()
        position = intent.extras["position"].toString().toInt()
        positionViewModel.setPosition(position)
        initViewModel(tags)
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        detailPager = findViewById(R.id.detail_pager)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar_layout)
        val toolbar = layoutInflater.inflate(R.layout.layout_toolbar, null)
        appBarLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_post))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        appBarLayout.addView(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { _, insets ->
            paddingTop = insets.systemWindowInsetTop
            paddingBottom = insets.systemWindowInsetBottom
            appBarLayout.minimumHeight = toolbar.minimumHeight + paddingTop
            toolbar.setPadding(0, paddingTop, 0, 0)
            appBarLayout.removeView(toolbar)
            appBarLayout.addView(toolbar)
            insets
        }
    }

    private fun initDetailPager() {
        detailAdapter = DetailAdapter(supportFragmentManager, mutableListOf(
                InfoFragment(),
                PagerFragment(),
                TagFragment()
        ))
        detailPager.adapter = detailAdapter
        detailPager.currentItem = 1
    }

    private fun initViewModel(tags: String) {
        if (tags == "") {
            type = "post"
            detailViewModel.postOutcome.observe(this, Observer<Outcome<MutableList<Post>>> { outcome: Outcome<MutableList<Post>>? ->
                when (outcome) {
                    is Outcome.Progress -> {

                    }
                    is Outcome.Success -> {
                        posts = outcome.data
                        initDetailPager()
                    }
                    is Outcome.Failure -> {

                    }
                }
            })
            detailViewModel.loadPosts(app.settings.activeProfileHost)
        } else {
            type = "search"
            detailViewModel.postSearchOutcome.observe(this, Observer<Outcome<MutableList<PostSearch>>> { outcome: Outcome<MutableList<PostSearch>>? ->
                when (outcome) {
                    is Outcome.Progress -> {

                    }
                    is Outcome.Success -> {
                        postsSearch = outcome.data
                        initDetailPager()
                    }
                    is Outcome.Failure -> {

                    }
                }
            })
            detailViewModel.loadPosts(app.settings.activeProfileHost, tags)
        }
    }

}