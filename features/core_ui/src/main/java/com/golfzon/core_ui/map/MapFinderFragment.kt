package com.golfzon.core_ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.databinding.FragmentMapFinderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFinderFragment : Fragment() {
    private var binding by autoCleared<FragmentMapFinderBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapFinderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWebView()
    }

    private fun setWebView() {
        val mWebViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url?.startsWith("https://m.golfzon.com/shop/#/main/") == true) {
                    view?.evaluateJavascript(
                        "javascript:(function() { " +
                                "var target = document.querySelector('.container');" +
                                "var observer = new MutationObserver(function(mutations) { " +
                                "   mutations.forEach(function(mutation) { " +
                                "       HTMLHandler.processHTML(target.outerHTML);" +
                                "   });" +
                                "});" +
                                "observer.observe(target, { childList: true, subtree: true });" +
                                "})();", null
                    )
                }

                super.onPageFinished(view, url)
            }
        }

        with(binding.webView) {
            apply {
                webViewClient = mWebViewClient
                settings.javaScriptEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun processHTML(html: String?) {
                        // TOOD 파싱
                    }
                }, "HTMLHandler")
                loadUrl("https://m.golfzon.com/shop/#/main/10519")
                this.visibility = View.VISIBLE
            }
        }
    }
}
