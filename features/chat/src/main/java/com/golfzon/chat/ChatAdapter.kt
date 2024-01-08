package com.golfzon.chat

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.databinding.ItemChatMineBinding
import com.golfzon.core_ui.databinding.ItemChatOtherBinding
import com.golfzon.core_ui.getColorHex
import com.golfzon.core_ui.map.CustomWebViewContainer
import com.golfzon.core_ui.map.WebViewTouchEventCallback
import com.golfzon.domain.model.ChatMessageType
import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.model.User
import java.io.IOException

class ChatAdapter(
    private val requestManager: RequestManager,
    private val userUId: String,
    private val membersInfo: List<User>
) :
    ListAdapter<GroupMessage, RecyclerView.ViewHolder>(diffCallback) {
    var onFirstRenderCompleted: (() -> Unit)? = null

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<GroupMessage>() {
            override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean =
                oldItem == newItem
        }
        private const val TYPE_MINE = 0
        private const val TYPE_OTHER = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType % 2) {
            TYPE_MINE -> {
                ChatViewMineHolder(
                    ItemChatMineBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                ChatViewOtherHolder(
                    ItemChatOtherBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is ChatViewMineHolder) {
            holder.bind(getItem(position))
        } else if (holder is ChatViewOtherHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun submitList(list: MutableList<GroupMessage>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCurrentListChanged(
        previousList: MutableList<GroupMessage>,
        currentList: MutableList<GroupMessage>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        if (previousList.isEmpty()) onFirstRenderCompleted?.invoke()
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).from == userUId) position * 2
        else position * 2 + 1

    private fun setMap(
        placeUId: String,
        webView: WebView,
        cardViewContainer: CustomWebViewContainer,
        topBarColor: String,
        topBarTextColor: String,
    ) {
        val golfzonWebViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request!!.url
                if (uri.toString().endsWith("pretendard_regular.otf")) {
                    try {
                        val stream = webView.context
                            .applicationContext.assets
                            .open("fonts/pretendard_regular.otf")
                        return WebResourceResponse("fonts/otf", "UTF-8", stream)
                    } catch (e: IOException) {
                        e.printStackTrace() // 글꼴 파일 로드 실패 처리
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val loadFontScript =
                    "var pretendard = new FontFace('PretendardRegular', 'url(pretendard_regular.otf)');" +
                            "pretendard.load().then(function(loadedFont) {" +
                            "    document.fonts.add(loadedFont);" +
                            "    document.body.style.fontFamily = 'PretendardRegular';" +
                            "}).catch(function(error) {" +
                            "    console.log('Failed to load Pretendard font: ' + error);" +
                            "});"
                val customTopBarScript = "javascript:(function() { " +
                        "var target = document.querySelector('.forweb');" +
                        "var observer = new MutationObserver(function(mutations) { " +
                        "   mutations.forEach(function(mutation) { " +
                        "       document.querySelector('.l__sub').style.backgroundColor = '$topBarColor';" +
                        "       document.querySelector('.l__sub').querySelector('h1').style.color = '$topBarTextColor';" +
                        "       document.querySelector('.btn_back').classList.remove('btn_back');" +
                        "       document.querySelector('h1').textContent ='[예약완료]';" +
                        "   });" +
                        "});" +
                        "observer.observe(target, { childList: true, subtree: true });" +
                        "})();"

                view?.let {
                    it.evaluateJavascript(loadFontScript, null)
                    it.evaluateJavascript(customTopBarScript, null)
                }
                super.onPageFinished(view, url)
            }
        }

        with(webView) {
            isEnabled = false
            setBackgroundColor(0)
            webViewClient = golfzonWebViewClient
            with(settings) {
                javaScriptEnabled = true
                allowFileAccess = true;
            }

            loadUrl("https://m.golfzon.com/booking/#/booking/map/view/${placeUId}")
        }

        cardViewContainer.setTouchEventCallback(object :
            WebViewTouchEventCallback {
            override fun onEvent(event: MotionEvent?) {
                // TODO Map Finder Fragment이동
            }
        })
    }

    inner class ChatViewOtherHolder(private val binding: ItemChatOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.requestManager = this@ChatAdapter.requestManager
            membersInfo.find { it.userUId == message.from }?.let {
                with(binding) {
                    memberInfo = it
                    messageInfo = message
                }

                if (message.type == ChatMessageType.RESERVATION) {
                    with(binding) {
                        setMap(
                            placeUId = message.placeUId,
                            webView = webviewChatOtherReservationMessage,
                            cardViewContainer = cardviewChatOtherReservationMessage,
                            topBarColor = ContextCompat.getColor(
                                webviewChatOtherReservationMessage.context,
                                com.golfzon.core_ui.R.color.gray_500_464B4B
                            ).getColorHex,
                            topBarTextColor = ContextCompat.getColor(
                                webviewChatOtherReservationMessage.context,
                                com.golfzon.core_ui.R.color.white
                            ).getColorHex,
                            topBarTextSize = 14
                        )
                    }
                }
            }
        }
    }

    inner class ChatViewMineHolder(private val binding: ItemChatMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.messageInfo = message

            if (message.type == ChatMessageType.RESERVATION) {
                with(binding) {
                    setMap(
                        placeUId = message.placeUId,
                        webView = webviewChatMineReservationMessage,
                        cardViewContainer = cardviewChatMineReservationMessage,
                        topBarColor = ContextCompat.getColor(
                            webviewChatMineReservationMessage.context,
                            com.golfzon.core_ui.R.color.primary_A4EF69
                        ).getColorHex,
                        topBarTextColor = ContextCompat.getColor(
                            webviewChatMineReservationMessage.context,
                            com.golfzon.core_ui.R.color.gray_700_272929
                        ).getColorHex,
                        topBarTextSize = 14
                    )
                }
            }
        }
    }
}