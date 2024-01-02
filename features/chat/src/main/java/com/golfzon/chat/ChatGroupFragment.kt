package com.golfzon.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.chat.databinding.FragmentChatGroupBinding
import com.golfzon.core_ui.KeyBoardUtil.hideKeyboard
import com.golfzon.core_ui.KeyboardVisibilityUtils
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
        binding.btnChatGroupAppbarBack.setOnDebounceClickListener { findNavController().navigateUp() }
    }

    private fun getCurrentUserInfo() {
        chatViewModel.getCurrentUserInfo()
    }

    private fun observePastMessages() {
        chatViewModel.chatLogs.observe(viewLifecycleOwner) {
            if (chatAdapter != null) {
                chatAdapter?.submitList(it)
                binding.rvChatGroupLog.scrollToPosition(chatAdapter!!.itemCount - 1)
            }
        }
    }

    private fun getNewMessage() {
        chatViewModel.receiveMessage(groupUId)
    }

    private fun observeNewMessage() {
        chatViewModel.newChat.observe(viewLifecycleOwner) {
            with(it.getContentIfNotHandled()) {
                if (this != null && chatAdapter != null) {
                    chatAdapter?.submitList(chatViewModel.chatLogs.value)
                    binding.rvChatGroupLog.smoothScrollToPosition(chatViewModel.chatLogs.value!!.size - 1)
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
        chatViewModel.chatGroupMembersInfo.observe(viewLifecycleOwner) {
            if (it.first.isEmpty() || it.second.first.isEmpty()) return@observe
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
            binding.rvChatGroupLog.adapter = chatAdapter
            observePastMessages()
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
                                com.golfzon.core_ui.R.color.primary_8B95B3
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

                        this.imageTintList = ContextCompat.getColorStateList(
                            requireContext(),
                            com.golfzon.core_ui.R.color.gray_C4C4C4
                        )
                    }
                }
            }
        }
    }
}