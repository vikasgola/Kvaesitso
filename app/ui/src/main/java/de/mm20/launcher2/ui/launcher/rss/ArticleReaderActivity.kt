package de.mm20.launcher2.ui.launcher.rss

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.base.BaseActivity
import de.mm20.launcher2.ui.base.ProvideCompositionLocals
import de.mm20.launcher2.ui.theme.LauncherTheme

class ArticleReaderActivity : BaseActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val url = intent.getStringExtra(EXTRA_URL)?.trim().orEmpty()
        if (url.isEmpty()) {
            finish()
            return
        }

        setContent {
            LauncherTheme {
                ProvideCompositionLocals {
                    val title = stringResource(R.string.article_reader_title)
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(title) },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.menu_back),
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            )
                        },
                    ) { padding ->
                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    webViewClient = ReaderWebViewClient()
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    @SuppressLint("SetJavaScriptEnabled")
                                    loadUrl(url)
                                }
                            },
                            update = { },
                        )
                    }
                }
            }
        }
    }

    private inner class ReaderWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val u = request.url.toString()
            if (u.startsWith("http://", ignoreCase = true) || u.startsWith("https://", ignoreCase = true)) {
                return false
            }
            return true
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest,
        ): WebResourceResponse? {
            val uri = request.url
            if (!request.isForMainFrame && AdBlockHosts.shouldBlock(uri)) {
                return WebResourceResponse("text/plain", "utf-8", java.io.ByteArrayInputStream(ByteArray(0)))
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            view.evaluateJavascript(COSMETIC_HIDE_SCRIPT, null)
        }
    }

    companion object {
        const val EXTRA_URL = "de.mm20.launcher2.rss.EXTRA_URL"

        private const val COSMETIC_HIDE_SCRIPT =
            """(function(){
              try {
                var s=document.createElement('style');
                s.textContent='iframe[src*="doubleclick"],iframe[src*="googlesyndication"],iframe[src*="adnxs"],[class*="ad-container"],[class*="ad_banner"],[id*="google_ads"],[id*="advert"],ins.adsbygoogle{display:none!important;visibility:hidden!important;max-height:0!important;overflow:hidden!important;}';
                (document.head||document.documentElement).appendChild(s);
              } catch(e) {}
            })();"""
    }
}
