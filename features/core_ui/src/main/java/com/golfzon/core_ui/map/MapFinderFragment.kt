package com.golfzon.core_ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.R
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.databinding.FragmentMapFinderBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class MapFinderFragment : Fragment() {
    private var binding by autoCleared<FragmentMapFinderBinding> { onDestroyBindingView() }
    private val args by navArgs<MapFinderFragmentArgs>()
    private var isClosed = false

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
        isClosed = false
    }
    private fun onDestroyBindingView() {
        isClosed = false
    }


    private fun setWebView() {
        val regexBase = "^https://m\\.golfzon\\.com/mapfinder/#/map/$".toRegex()
        val regexExtended = "^https://m\\.golfzon\\.com/mapfinder/#/map/.+".toRegex()
        val regexExtendedShop = "^https://m\\.golfzon\\.com/shop/#/main/.+".toRegex()

        val mWebViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request?.url?.toString()?.matches(regexExtendedShop) == true && args.recruitPlaceUId != "") {
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (url?.startsWith("https://m.golfzon.com/shop/#/main/") == true) {
                    view?.visibility = View.INVISIBLE // 되돌아갈때 보이지 않도록 처리

                    // 세부정보 페이지 로딩 완료후 콘텐츠 파싱
                    view?.evaluateJavascript(
                        "javascript:(function() { " +
                                "var target = document.querySelector('.container');" +
                                "var observer = new MutationObserver(function(mutations) { " +
                                "   mutations.forEach(function(mutation) { " +
                                "       HTMLHandler.processHTML(target.querySelector('.shop_summary').firstChild.textContent, ${url.split("/").last()}, target.querySelector('.shop_summary').querySelector('.add').textContent.split('(지번)') );" +
                                "   });" +
                                "});" +
                                "observer.observe(target, { childList: true, subtree: true });" +
                                "})();", null
                    )
                } else if (url?.matches(regexExtended) == true) {
                    // 이미 장소가 입력된 경우
                    // 골프장 검색, 찜, 현재 위치 버튼 제거
                    view?.evaluateJavascript(
                        "javascript:(function() { " +
                                "var target = document.querySelector('.shop_find_wrap.sitefinder_home');" +
                                "var observer = new MutationObserver(function(mutations) { " +
                                "   mutations.forEach(function(mutation) { " +
                                "       var elementsToRemove = [" +
                                "           target.querySelector('.shop_find_wrap.sitefinder_home')," +
                                "           target.querySelector('.gpsbtn')," +
                                "           target.lastElementChild];" +
                                "       elementsToRemove.forEach(function(element) {" +
                                "           if (element && target.contains(element) && target.childElementCount >= 3) {" +
                                "               target.removeChild(element);" +
                                "           }" +
                                "       });" +
                                "       if (document.querySelector('.shop_list_btn') != null) {" +
                                "           document.querySelector('.shop_list_cont').removeChild(document.querySelector('.shop_list_btn'))" +
                                "       }" +
                                "       if (document.querySelector('.shop_list_cont.bottom_line') != null) {" +
                                "           document.querySelectorAll('.shop_list_cont.bottom_line').forEach(function(element) {" +
                                "              if(element.querySelector('.shop_list_btn') != null) {" +
                                "                  element.removeChild(element.querySelector('.shop_list_btn'));" +
                                "              }" +
                                "          });" +
                                "       }" +
                                "   });" +
                                "});" +
                                "observer.observe(target, { childList: true, subtree: true });" +
                                "})();", null
                    )
                } else if (url?.matches(regexBase) == true)  {
                    // 최초 장소 검색
                    // 찜, 현재 위치 버튼 제거
                    view?.evaluateJavascript(
                        "javascript:(function() { " +
                                "var target = document.querySelector('.shop_find_wrap.sitefinder_home');" +
                                "var observer = new MutationObserver(function(mutations) { " +
                                "   mutations.forEach(function(mutation) { " +
                                "       var elementsToRemove = [" +
                                "           target.querySelector('.gpsbtn')," +
                                "           target.lastElementChild];" +
                                "       elementsToRemove.forEach(function(element) {" +
                                "           if (element && target.contains(element) && target.childElementCount >= 3) {" +
                                "               target.removeChild(element);" +
                                "           }" +
                                "       });" +
                                "       if (document.querySelector('.shop_list_btn') != null) {" +
                                "           document.querySelector('.shop_list_cont').removeChild(document.querySelector('.shop_list_btn'))" +
                                "       }" +
                                "       if (document.querySelector('.shop_list_cont.bottom_line') != null) {" +
                                "           document.querySelectorAll('.shop_list_cont.bottom_line').forEach(function(element) {" +
                                "              if(element.querySelector('.shop_list_btn') != null) {" +
                                "                  element.removeChild(element.querySelector('.shop_list_btn'));" +
                                "              }" +
                                "          });" +
                                "       }" +
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
                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun processHTML(placeName: String, placeUId: String, places: Array<String>) {
                        try {
                            // requireView 등을 사용하는 경우, 단순히 사용하지 않을뿐만 아니라 Exception을 Throw하기 때문에 view에 대한 null 체크를 직접 실행
                            if (view != null && requireView().findNavController().currentBackStack.value.size == 4 && requireView().findNavController().currentDestination?.id == R.id.MapFinderFragment) {
                                requireActivity().runOnUiThread {
                                    view?.let { notNullView ->
                                        notNullView.findNavController().previousBackStackEntry?.savedStateHandle?.set("recruitPlaceName",placeName )
                                        notNullView.findNavController().previousBackStackEntry?.savedStateHandle?.set("recruitPlaceUId", placeUId)
                                        notNullView.findNavController().previousBackStackEntry?.savedStateHandle?.set("recruitPlaceRoadAddress", places[0].trim())
                                        notNullView.findNavController().previousBackStackEntry?.savedStateHandle?.set("recruitPlacePastAddress", places[1].trim())
                                        if (!isClosed) {
                                            notNullView.findNavController().popBackStack()
                                            isClosed = true
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, "HTMLHandler")
                loadUrl("https://m.golfzon.com/mapfinder/#/map/${args.recruitPlaceUId}")
                this.visibility = View.VISIBLE
            }
        }
    }
}
