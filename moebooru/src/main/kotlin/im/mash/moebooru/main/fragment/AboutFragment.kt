package im.mash.moebooru.main.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import im.mash.moebooru.BuildConfig
import im.mash.moebooru.R
import im.mash.moebooru.common.base.ToolbarFragment
import im.mash.moebooru.main.MainActivity
import im.mash.moebooru.util.launchUrl

class AboutFragment : ToolbarFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val configuration = resources.configuration
        val view = inflater.inflate(R.layout.layout_about, container, false)
        if (resources.configuration != configuration) {
            (activity as MainActivity).recreate()
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            view.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets
        }
        view.setBackgroundColor(ContextCompat.getColor(activity as MainActivity, R.color.window_background))
        toolbar.title = getString(R.string.about_title) + " " + BuildConfig.VERSION_NAME
        val web = view.findViewById<WebView>(R.id.web_view)
        web.setBackgroundColor(ContextCompat.getColor(activity as MainActivity, R.color.background))
        web.loadUrl(getString(R.string.about_url))
        web.webViewClient = object : WebViewClient() {

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                (activity as MainActivity).launchUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                (activity as MainActivity).launchUrl(request.url)
                return true
            }
        }
    }

}