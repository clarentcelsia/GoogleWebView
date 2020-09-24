package com.example.webview

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.IpSecManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


// ACCESS GOOGLE AND BACK TO PREVIOUS PAGE
class MainActivity : AppCompatActivity() {

    private val link = "https://www.google.com"
    private val CUSTOM_PACKAGE = "com.android.chrome"

    var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null
    var customTabsServiceConnection: CustomTabsServiceConnection? = null
    var customTabsIntent: CustomTabsIntent? = null

    lateinit var objectAnimator: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etSearch.setText(link)
        webview.apply {
            //create client type : webview / chrome

            // webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            //what url do you wanna load
            loadUrl(link)
        }

        goBack()
        goForward()
        goHome()

        setListener()

        //Chrome custom tabs
//        setupTabServiceConn()
//        btnTab.setOnClickListener{
//            customTabsIntent?.launchUrl(this, Uri.parse(link))
//        }

        btnTab.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
    }

    fun setupTabServiceConn() {
        customTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                customTabsClient = client
                customTabsClient?.warmup(0L)
                customTabsSession = customTabsClient?.newSession(null)
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                customTabsClient = null
            }

        }

        CustomTabsClient.bindCustomTabsService(
            this,
            CUSTOM_PACKAGE,
            customTabsServiceConnection as CustomTabsServiceConnection
        )
        customTabsIntent = CustomTabsIntent.Builder(customTabsSession)
            .setShowTitle(true)
            .setToolbarColor(Color.DKGRAY)
            .build()
    }

    // Setup Progress Bar
    fun setProgressBar() {
        //set progress max
        progressbar.max = 1000

        // objectAnim (progressbar, string name, start progress at, end at..)
        objectAnimator = ObjectAnimator.ofInt(progressbar, "progress", 0, 1000)
        objectAnimator.apply {
            duration = 2200
            interpolator = DecelerateInterpolator()
            start()

        }
    }

    fun dismissProgress() {
        objectAnimator.removeAllListeners()
        objectAnimator.end()

        objectAnimator = ObjectAnimator.ofInt(progressbar, "progress", 0, 0)
        objectAnimator.apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
            end()
        }
    }

    // Setup Search
    fun setListener() {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val getUrl = etSearch.text.toString()

                    webview.visibility = View.GONE
                    setProgressBar()
                    Handler().postDelayed(Runnable {
                        webview.loadUrl("http://www.google.com/search?ie=UTF-8&client=ms-android-samsung-gj-rev1&source=android-browser&q=${getUrl}")
                        dismissProgress()
                        webview.visibility = View.VISIBLE
                    }, 800)
                    true
                }
                else -> false

            }
        }
    }


    // Navigating web page history
    fun goBack() {
        btnBack.setOnClickListener {
            setProgressBar()
            Handler().postDelayed(Runnable {
                if (webview.canGoBack()) webview.goBack()
                dismissProgress()
            }, 1000)
        }
    }

    fun goForward() {
        btnForward.setOnClickListener {
            setProgressBar()
            Handler().postDelayed(Runnable {
                if (webview.canGoForward()) webview.goForward()
                dismissProgress()
            }, 800)
        }
    }

    fun goHome() {
        btnHome.setOnClickListener {
            setProgressBar()
            Handler().postDelayed(Runnable {
                webview.clearHistory()
                webview.loadUrl(link)
                dismissProgress()
            },900)

        }
    }

    override fun onBackPressed() {
        //set view back to previous pages after load many pages
        if (webview.canGoBack()) webview.goBack()
        else super.onBackPressed()
    }
}

/* WEB VIEW CLIENT / WEB CHROME CLIENT
   1. if you are developing a WebView that won't require too many features but rendering HTML, you can just use a [ WebViewClient ]
   2. if you want to (for instance) load the favicon(tab/website icon) of the page you are rendering, --> tampilan web desktop
   3. Use a [ WebChromeClient ] object and override the onReceivedIcon(WebView view, Bitmap icon).
 */

/* THREAD VS ASYNC
 btnBack.setOnClickListener {

            progressbar.visibility = View.VISIBLE
            var progress = 10
            Thread(Runnable {

                while (progress < 100){
                    progress += 5+2
                    // Concept : Asynctask
                    // Asynctask -> runOnUIThread
                    // Handler -> Looper.getMainLooper
                    Handler(Looper.getMainLooper()).post{
                        progressbar.progress = progress
                        if (webview.canGoBack()) webview.goBack()
                    }

                    try {
                        Thread.sleep(300L)
                    }catch (e: InterruptedException){
                        e.printStackTrace()
                    }
                }

            }).start()

        }
 */

