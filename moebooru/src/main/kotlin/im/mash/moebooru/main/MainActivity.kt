package im.mash.moebooru.main

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatDelegate
import android.support.v7.content.res.AppCompatResources
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import im.mash.moebooru.App.Companion.app
import im.mash.moebooru.R
import im.mash.moebooru.Settings
import im.mash.moebooru.common.MoeDH
import im.mash.moebooru.common.base.ToolbarFragment
import im.mash.moebooru.common.data.local.MoeDatabase
import im.mash.moebooru.common.data.local.entity.Booru
import im.mash.moebooru.common.data.local.entity.User
import im.mash.moebooru.common.data.remote.PostSearchService
import im.mash.moebooru.common.viewmodel.DownloadViewModelFactory
import im.mash.moebooru.common.viewmodel.UserViewModel
import im.mash.moebooru.common.viewmodel.UserViewModelFactory
import im.mash.moebooru.common.viewmodel.VoteViewModelFactory
import im.mash.moebooru.core.application.BaseActivity
import im.mash.moebooru.core.extensions.performOnBackOutOnMain
import im.mash.moebooru.core.scheduler.Outcome
import im.mash.moebooru.core.scheduler.Scheduler
import im.mash.moebooru.glide.GlideApp
import im.mash.moebooru.glide.MoeGlideUrl
import im.mash.moebooru.helper.getViewModel
import im.mash.moebooru.main.fragment.*
import im.mash.moebooru.main.viewmodel.*
import im.mash.moebooru.util.ColorUtil
import im.mash.moebooru.util.TextUtil
import im.mash.moebooru.util.isNetworkConnected
import im.mash.moebooru.util.logi
import io.reactivex.Completable
import okhttp3.HttpUrl
import retrofit2.HttpException
import java.io.*
import javax.inject.Inject

@SuppressLint("RtlHardcoded")
class MainActivity : BaseActivity(), Drawer.OnDrawerItemClickListener, DrawerLayout.DrawerListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val DRAWER_ITEM_POSTS = 0L
        private const val DRAWER_ITEM_POOLS = 1L
        private const val DRAWER_ITEM_ACCOUNT = 2L
        private const val DRAWER_ITEM_DOWNLOADS = 3L
        private const val DRAWER_ITEM_LOCAL_GALLERY = 4L
        private const val DRAWER_ITEM_SETTINGS = 5L
        private const val DRAWER_ITEM_FEEDBACK = 6L
        private const val DRAWER_ITEM_ABOUT = 7L
        private const val SETTING_PROFILE_ID = 100L
    }

    internal lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem
    private var previousSelectedDrawer: Long = 0L
    private var selectedNewItem = false

    private val mainComponent by lazy { MoeDH.mainComponent() }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var booruViewModelFactory: BooruViewModelFactory
    @Inject
    lateinit var postViewModelFactory: PostViewModelFactory
    @Inject
    lateinit var poolViewModelFactory: PoolViewModelFactory
    @Inject
    lateinit var tagViewModelFactory: TagViewModelFactory
    @Inject
    lateinit var mediaViewModelFactory: MediaViewModelFactory
    @Inject
    lateinit var downloadViewModelFactory: DownloadViewModelFactory
    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    @Inject
    lateinit var voteViewModelFactory: VoteViewModelFactory

    private val userViewModel: UserViewModel by lazy { this.getViewModel<UserViewModel>(userViewModelFactory) }
    private var users: MutableList<User> = mutableListOf()

    private val booruViewModel: BooruViewModel by lazy { this.getViewModel<BooruViewModel>(booruViewModelFactory) }
    internal var boorus: MutableList<Booru> = mutableListOf()

    @Inject
    lateinit var database: MoeDatabase
    @Inject
    lateinit var postSearchService: PostSearchService
    @Inject
    lateinit var scheduler: Scheduler

    private var isNullState = true
    private var isNewCreate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_moebooru)

        mainComponent.inject(this)

        isNullState = savedInstanceState == null

        profileSettingDrawerItem = ProfileSettingDrawerItem()
                .withName(R.string.edit)
                .withIdentifier(SETTING_PROFILE_ID)
                .withIcon(R.drawable.ic_drawer_settings_24dp)

        header = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.background_header)
                .withOnAccountHeaderListener { _, profile, _ ->
                    val id = profile.identifier
                    if (id != SETTING_PROFILE_ID) {
                        app.settings.activeProfileId = id
                        val schema = boorus[id.toInt()].scheme
                        val host = boorus[id.toInt()].host
                        app.settings.activeProfileSchema = schema
                        app.settings.activeProfileHost = host
                        setHeaderBackground(schema, host)
                    } else {
                        drawer.setSelection(-1L)
                        previousSelectedDrawer = -1L
                        displayFragment(BooruFragment())
                    }
                    false
                }
                .build()

        drawer = DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withDrawerGravity(Gravity.LEFT)
                .withAccountHeader(header, false)
                .addDrawerItems(
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_POSTS)
                                .withName(R.string.posts)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_photo_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_POOLS)
                                .withName(R.string.pools)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_photo_album_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_ACCOUNT)
                                .withName(R.string.title_account)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_account_circle_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_DOWNLOADS)
                                .withName(R.string.downloads)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_file_download_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_LOCAL_GALLERY)
                                .withName(R.string.local_gallery)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_local_library_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_SETTINGS)
                                .withName(R.string.settings)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_settings_24dp))
                                .withIconTintingEnabled(true)

                )
                .addStickyDrawerItems(
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_FEEDBACK)
                                .withName(R.string.feedback)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_feedback_24dp))
                                .withIconTintingEnabled(true),
                        PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ITEM_ABOUT)
                                .withName(R.string.about)
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_drawer_copyright_24dp))
                                .withIconTintingEnabled(true)
                )
                .withStickyFooterDivider(true)
                .withStickyFooterShadow(false)
                .withOnDrawerItemClickListener(this)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSavedInstance(savedInstanceState)
                .build()

        ViewCompat.setOnApplyWindowInsetsListener(drawer.drawerLayout) { _, insets ->
            drawer.stickyFooter.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets
        }

        drawer.drawerLayout.addDrawerListener (this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        initBoorus()

        if (!isNullState && app.settings.isChangedNightMode) {
            drawer.setSelection(DRAWER_ITEM_SETTINGS)
            app.settings.isChangedNightMode = false
        }
    }

    private fun initBoorus() {
        booruViewModel.booruOutcome.observe(this, Observer<Outcome<MutableList<Booru>>> { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    logi(TAG, "boorus outcome progress")
                }
                is Outcome.Success -> {
                    logi(TAG, "boorus outcome success")
                    boorus = outcome.data
                    initUser()
                    booruChangeListener?.onBooruChanged(boorus)
                }
                is Outcome.Failure -> {
                    logi(TAG, "boorus outcome failed")
                    if (outcome.e is IOException) {
                        outcome.e.printStackTrace()
                    }
                }
            }
        })
        booruViewModel.loadBoorus()
    }

    private fun initUser() {
        var init = true
        userViewModel.userOutcome.observe(this, Observer<Outcome<MutableList<User>>> { outcome ->
            when (outcome) {
                is Outcome.Progress -> {}
                is Outcome.Success -> {
                    users.clear()
                    if (outcome.data.size > 0) {
                        users = outcome.data
                        if (this.isNetworkConnected && init) {
                            logi(TAG, "User Outcome success. init")
                            init = false
                            users.forEach { user ->
                                val url = user.url + "/data/avatars/" + user.id + ".jpg"
                                GlideApp.with(app)
                                        .asBitmap()
                                        .load(MoeGlideUrl(url))
                                        .into(object : SimpleTarget<Bitmap>() {
                                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                val os = ByteArrayOutputStream()
                                                resource.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                                val avatar = os.toByteArray()
                                                if (user.avatar == null || avatar.size != user.avatar!!.size) {
                                                    user.avatar = avatar
                                                    userViewModel.updateUser(user)
                                                }
                                            }
                                        })
                            }
                        }
                    }
                    logi(TAG, "User Outcome success")
                    initHeaderItem()
                }
                is Outcome.Failure -> {
                    initHeaderItem()
                    outcome.e.printStackTrace()
                }
            }
        })
        userViewModel.loadUsers()
    }

    internal fun initHeaderItem() {
        if (boorus.size <= 0) {
            header.clear()
            header.addProfile(profileSettingDrawerItem, 0)
            app.settings.activeProfileSchema = "http"
            app.settings.activeProfileHost = "mash.im"
            if (isNullState && isNewCreate) {
                isNewCreate = false
                displayFragment(BooruFragment())
            }

        } else {
            header.clear()
            val size = boorus.size
            for (index in 0 until size) {
                val booru = boorus[index]
                val profileDrawerItem: ProfileDrawerItem = ProfileDrawerItem()
                        .withName(booru.name)
                        .withEmail(booru.url)
                        .withIdentifier(index.toLong())
                var user: User? = null
                users.forEach { userOut ->
                    if (userOut.url == booru.url) {
                        user = userOut
                        return@forEach
                    }
                }
                if (user != null) {
                    val name = user!!.name
                    val avatar = user!!.getAvatarBitmap()
                    profileDrawerItem.withName(name).withIcon(avatar)
                    header.addProfile(profileDrawerItem, index)
                } else {
                    val text = booru.name[0].toString()
                    val icon = TextUtil.textDrawableBuilder().buildRound(text, ColorUtil.getCustomizedColor(this, text))
                    profileDrawerItem.withIcon(icon)
                    header.addProfile(profileDrawerItem, index)
                }
            }
            header.addProfile(profileSettingDrawerItem, size)
            val activeProfileId = app.settings.activeProfileId
            if (activeProfileId < size) {
                header.setActiveProfile(activeProfileId)
                val schema = boorus[activeProfileId.toInt()].scheme
                val host = boorus[activeProfileId.toInt()].host
                app.settings.activeProfileSchema = schema
                app.settings.activeProfileHost = host
                setHeaderBackground(schema, host)
            }
            if (isNullState && isNewCreate) {
                isNewCreate = false
                displayFragment(PostFragment())
            }
        }
    }

    internal fun setUsers(users: MutableList<User>) {
        this.users = users
    }

    internal fun setHeaderBackground(schema: String, host: String) {
        logi(TAG, "setHeaderBackground")
        var activeUser: User? = null
        val baseUrl = "$schema://$host"
        users.forEach { user ->
            if (user.url == baseUrl) {
                activeUser = user
                return@forEach
            }
        }
        if (activeUser != null) {
            val username = activeUser!!.name
            val passwordHash = activeUser!!.password_hash
            val keyword = "vote:3:$username order:vote"
            val dir = File(cacheDir, "background")
            if (!dir.exists()) {
                dir.mkdir()
            }
            var notBg = false
            val bg = File(dir, "$host-$username.jpg")
            if (bg.exists()) {
                GlideApp.with(app)
                        .asBitmap()
                        .load(Uri.fromFile(bg))
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                header.headerBackgroundView.setImageBitmap(resource)
                            }
                        })
            } else {
                notBg = true
                header.headerBackgroundView.setImageResource(R.drawable.background_header)
            }

            if (!this.isNetworkConnected) return

            val limit = app.settings.postLimitInt

            val httpUrl = HttpUrl.Builder()
                    .scheme(schema)
                    .host(host)
                    .addPathSegment("post.json")
                    .addQueryParameter("limit", limit.toString())
                    .addQueryParameter("page", "1")
                    .addQueryParameter("tags", keyword)
                    .addQueryParameter("login", username)
                    .addQueryParameter("password_hash", passwordHash)
                    .build()

            postSearchService.getPosts(httpUrl)
                    .performOnBackOutOnMain(scheduler)
                    .doAfterSuccess { posts ->
                        if (posts.size > 0) {
                            Completable.fromAction { database.postSearchDao().insertPosts(posts) }
                                    .performOnBackOutOnMain(scheduler)
                                    .doOnComplete {
                                        database.postSearchDao()
                                                .getLastPost(host, keyword)
                                                .performOnBackOutOnMain(scheduler)
                                                .doAfterNext { postSearch ->
                                                    if (postSearch != null) {
                                                        val url = postSearch.sample_url
                                                        GlideApp.with(app)
                                                                .asBitmap()
                                                                .load(MoeGlideUrl(url))
                                                                .into(object : SimpleTarget<Bitmap>() {
                                                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                                        var fos: FileOutputStream? = null
                                                                        try {
                                                                            fos = FileOutputStream(bg)
                                                                            resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                                                            fos.flush()
                                                                        } catch (e: FileNotFoundException) {
                                                                            e.printStackTrace()
                                                                        } catch (e: IOException) {
                                                                            e.printStackTrace()
                                                                        } finally {
                                                                            try {
                                                                                fos?.close()
                                                                            } catch (e: IOException) {
                                                                                e.printStackTrace()
                                                                            }
                                                                            if (notBg) header.headerBackgroundView.setImageBitmap(resource)
                                                                        }
                                                                    }
                                                                })
                                                    }
                                                }
                                                .doOnError { error -> error.printStackTrace() }
                                                .subscribe()
                                    }
                                    .subscribe()
                        }

                    }
                    .doOnError { error ->
                        if (error is HttpException) {
                            Toast.makeText(this, "code: ${error.code()}, msg: ${error.message()}", Toast.LENGTH_SHORT).show()
                        }
                        error.printStackTrace()
                    }
                    .subscribe()
        } else {
            header.headerBackgroundView.setImageResource(R.drawable.background_header)
        }
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_moebooru, fragment)
                .commitAllowingStateLoss()
        drawer.closeDrawer()
    }

    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {
        val id = drawerItem!!.identifier
        if (id != previousSelectedDrawer) {
            selectedNewItem = true
            previousSelectedDrawer = id
        }
        drawer.closeDrawer()
        return true
    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerClosed(drawerView: View) {
        if (selectedNewItem) {
            selectedNewItem = false
            when (previousSelectedDrawer) {
                DRAWER_ITEM_POSTS -> displayFragment(PostFragment())
                DRAWER_ITEM_POOLS -> displayFragment(PoolFragment())
                DRAWER_ITEM_ACCOUNT -> displayFragment(AccountFragment())
                DRAWER_ITEM_DOWNLOADS -> displayFragment(DownloadFragment())
                DRAWER_ITEM_LOCAL_GALLERY -> displayFragment(GalleryFragment())
                DRAWER_ITEM_SETTINGS -> displayFragment(SettingsFragment())
                DRAWER_ITEM_FEEDBACK -> displayFragment(FeedbackFragment())
                DRAWER_ITEM_ABOUT -> displayFragment(AboutFragment())
            }
        }
    }

    override fun onDrawerOpened(drawerView: View) {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.NIGHT_MODE -> {
                AppCompatDelegate.setDefaultNightMode(app.settings.nightMode)
                app.settings.isChangedNightMode = true
                recreate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_moebooru) as ToolbarFragment
            if (!currentFragment.onBackPressed()) {
                if (currentFragment is PostFragment) {
                    super.onBackPressed()
                } else {
                    drawer.setSelection(DRAWER_ITEM_POSTS)
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var result = true
        try {
            result = super.dispatchTouchEvent(ev)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return result
    }

    fun getHashSalt(): String {
        val position = app.settings.activeProfileId.toInt()
        if (position >= boorus.size) return ""
        return boorus[position].hash_salt
    }

    fun getUser(): User? {
        var user: User? = null
        val host = app.settings.activeProfileHost
        val schema = app.settings.activeProfileSchema
        val baseUrl = "$schema://$host"
        users.forEach {  u ->
            if (u.url == baseUrl) {
                user = u
                return@forEach
            }
        }
        return user
    }

    private var booruChangeListener: BooruChangeListener? = null

    fun setBooruChangeListener(booruChangeListener: BooruChangeListener?) {
        this.booruChangeListener = booruChangeListener
    }

    interface BooruChangeListener {
        fun onBooruChanged(boorus: MutableList<Booru>)
    }

    fun getBoorus(): MutableList<Booru> {
        return boorus
    }

    fun addBooru(booru: Booru) {
        booruViewModel.addBooru(booru)
    }

    fun deleteBooru(booru: Booru) {
        booruViewModel.deleteBooru(booru)
    }
}
