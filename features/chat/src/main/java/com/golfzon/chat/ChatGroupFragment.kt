package com.golfzon.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.chat.databinding.FragmentChatGroupBinding
import com.golfzon.core_ui.KeyBoardUtil.hideKeyboard
import com.golfzon.core_ui.KeyboardVisibilityUtils
import com.golfzon.core_ui.R
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.metrics.Trace
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatGroupFragment : Fragment() {
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private var binding by autoCleared<FragmentChatGroupBinding> { onDestroyBindingView() }
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private var glideRequestManager: RequestManager? = null
    private var chatConcatAdapter: ConcatAdapter? = null
    private var chatHeaderAdapter: ChatHeaderAdapter? = null
    private var chatAdapter: ChatAdapter? = null
    private val groupUId by lazy {
        (requireActivity() as ChatActivity).intent.getStringExtra("groupUId") ?: ""
    }

    private val chatLogLoadingTrace: Trace = Firebase.performance.newTrace("chat_log_loading_trace")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChatLogLoadingTrace()
    }

    private fun setChatLogLoadingTrace() {
        with(chatLogLoadingTrace) {
            incrementMetric("chat_log_loading_count", 1)
            putAttribute("groupUId", groupUId)
            start()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatGroupBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@ChatGroupFragment)
        setDataBindingVariables()
        getGroupDetail()
        getCurrentUserInfo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackClickListener()
        setChatLogAdapter()
        setHideKeyboard()
        setMessageInputClickListener()
        setMessageSendClickListener()
    }

    private fun onDestroyBindingView() {
        keyboardVisibilityUtils.detachKeyboardListeners()
        chatConcatAdapter = null
        chatHeaderAdapter = null
        chatAdapter = null
        glideRequestManager = null
        chatViewModel.removeChatListener()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = chatViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBackClickListener() {
        binding.btnChatGroupAppbarBack.setOnDebounceClickListener {
            (requireActivity() as ChatActivity).finish()
        }
    }

    private fun getCurrentUserInfo() {
        chatViewModel.getCurrentUserInfo()
    }

    private fun getNewMessage() {
        chatViewModel.receiveMessage(groupUId)
    }

    private fun observeNewMessage() {
        chatViewModel.chatLogs.observe(viewLifecycleOwner) {
            with(it) {
                if (this != null && chatAdapter != null) {
                    chatAdapter?.submitList(it)
                    // 메시지 추가 직전에 마지막 item을 보고있었고, 새로운 채팅이 추가된 경우에만 자동 스크롤
                    if ((binding.rvChatGroupLog.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                        == (binding.rvChatGroupLog.adapter?.itemCount ?: 1) - 1
                    ) {
                        if (it.size > 1) {
                            binding.rvChatGroupLog.smoothScrollToPosition(it.size - 1)
                        }
                    }
                }
            }
        }
    }

    private fun getGroupDetail() {
        with(chatViewModel) {
            getChatMembersInfo()
            getGroupInfo(groupUId)
        }
    }

    private fun setChatLogAdapter() {
        var renderedItemCount = 0

        chatViewModel.chatGroupMembersInfo.observe(viewLifecycleOwner) {
            if (it.first.isEmpty() || it.second.first.isEmpty()) return@observe
            setTitleChatMembersCount(it.first.size)
            chatHeaderAdapter = ChatHeaderAdapter(groupUId, chatViewModel.groupDetailInfo.value).apply {
                setOnItemClickListener(object : ChatHeaderAdapter.OnItemClickListener {
                    override fun reservationScreen(groupUId: String, pos: Int) {
                        (requireActivity() as ChatActivity).navigateToGroup(
                            destination = getString(com.golfzon.core_ui.R.string.group_reservation_deeplink_url),
                            groupUId = groupUId
                        )
                    }
                })
            }

            chatAdapter = ChatAdapter(
                requestManager = glideRequestManager!!,
                userUId = it.second.first,
                membersInfo = it.first
            ).apply {
                setHasStableIds(true)
                onFirstRenderCompleted = {
                    if (this.itemCount > 0) {
                        binding.rvChatGroupLog.let { rv ->
                            rv.scrollToPosition(this.itemCount - 1)
                        }
                        chatLogLoadingTrace.let { trace ->
                            trace.putAttribute("chatLogCount", this.itemCount.toString())
                            trace.stop()
                        }
                    }
                }
            }
            chatConcatAdapter = ConcatAdapter(chatHeaderAdapter!!, chatAdapter!!)
            with(binding.rvChatGroupLog) {
                adapter = chatConcatAdapter
                layoutManager = object : LinearLayoutManager(requireContext()) {
                    override fun onLayoutCompleted(state: RecyclerView.State?) {
                        super.onLayoutCompleted(state)

                        // 10개 이상 채팅이 있는 경우, 10개이상 렌더링 된 경우에만 보이도록 설정, 이하인 경우 절반 이상이 렌더링 된경우에만 보이도록 설정
                        renderedItemCount++
                        val itemCount = this@with.adapter?.itemCount ?: return

                        with(this@with) {
                            isVisible = when {
                                itemCount <= 0 -> false
                                itemCount >= 10 -> renderedItemCount >= 10
                                else -> renderedItemCount > (itemCount / 2)
                            }
                        }
                    }
                }
            }
            getNewMessage()
            observeNewMessage()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setHideKeyboard() {
        var startClickTime = 0L;
        with(binding) {
            etChatGroupUserInput.hideKeyboard(requireActivity())
            rvChatGroupLog.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_DOWN) {
                        startClickTime = System.currentTimeMillis()
                    } else if (event?.action == MotionEvent.ACTION_UP) {
                        if (System.currentTimeMillis() - startClickTime < ViewConfiguration.getTapTimeout()) {
                            etChatGroupUserInput.hideKeyboard(requireActivity())
                        }
                    }
                    return false
                }
            })
        }
    }

    private fun setMessageInputClickListener() {
        keyboardVisibilityUtils = KeyboardVisibilityUtils(requireActivity().window,
            onShowKeyboard = { keyboardHeight ->
                binding.rvChatGroupLog.run {
                    smoothScrollBy(
                        scrollX,
                        scrollY + keyboardHeight - binding.layoutChatGroupUserInput.height + 15.dp
                    )
                }
            }
        )
    }

    private fun setMessageSendClickListener() {
        with(binding.etChatGroupUserInput) {
            hideKeyboard(requireContext())
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.trim().isNullOrEmpty()) {
                        binding.btnChatGroupUserInputSend.imageTintList =
                            ContextCompat.getColorStateList(
                                requireContext(),
                                com.golfzon.core_ui.R.color.gray_C4C4C4
                            )
                    } else {
                        binding.btnChatGroupUserInputSend.imageTintList =
                            ContextCompat.getColorStateList(
                                requireContext(),
                                com.golfzon.core_ui.R.color.primary_A4EF69
                            )
                    }
                }
            })
        }

        with(binding.btnChatGroupUserInputSend) {
            this.setOnDebounceClickListener {
                binding.etChatGroupUserInput.text.let { inputText ->
                    if (inputText.trim().isNotEmpty()) {
                        chatViewModel.sendMessage(groupUId, inputText.toString())
                        inputText.clear()

                        this.imageTintList = ContextCompat.getColorStateList(requireContext(),R.color.gray_C4C4C4)
                    }
                }
            }
        }
    }

    private fun setTitleChatMembersCount(membersCount: Int) {
        val titleSpan = SpannableString(getString(com.golfzon.chat.R.string.chat_members_count, membersCount))
        val membersCountToString = membersCount.toString()
        titleSpan.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.primary_A4EF69, null)),
            titleSpan.indexOf(membersCountToString),
            titleSpan.indexOf(membersCountToString) + membersCountToString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvChatGroupAppbarTitle.text = titleSpan
    }
}