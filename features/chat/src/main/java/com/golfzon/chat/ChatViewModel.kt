package com.golfzon.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.model.TextMessage
import com.golfzon.domain.model.User
import com.golfzon.domain.repository.OnGrpMessageResponse
import com.golfzon.domain.usecase.chat.GetGroupMessageUseCase
import com.golfzon.domain.usecase.chat.GetPastGroupMessageUseCase
import com.golfzon.domain.usecase.chat.SendGroupMessageUseCase
import com.golfzon.domain.usecase.group.GetGroupDetailUseCase
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getCurUserInfoUseCase: GetCurUserInfoUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase,
    private val getGroupMessageUseCase: GetGroupMessageUseCase,
    private val getPastGroupMessageUseCase: GetPastGroupMessageUseCase
) : ViewModel() {

    private val _currentUserBasicInfo =
        MutableStateFlow<Triple<String, String, String>>(Triple("", "", ""))
    val currentUserBasicInfo: StateFlow<Triple<String, String, String>> get() = _currentUserBasicInfo

    private val _groupMembersInfo = MutableStateFlow<List<User>>(listOf())
    val groupMembersInfo: StateFlow<List<User>> get() = _groupMembersInfo

    private val _chatGroupMembersInfo = MutableLiveData<Pair<List<User>, Triple<String, String, String>>>(Pair(listOf(), Triple("", "", "")))
    val chatGroupMembersInfo: LiveData<Pair<List<User>, Triple<String, String, String>>> get() = _chatGroupMembersInfo

    private val _chatLogs = ListLiveData<GroupMessage>()
    val chatLogs: ListLiveData<GroupMessage> = _chatLogs

    private val _newChat = MutableLiveData<Event<GroupMessage>>()
    val newChat: LiveData<Event<GroupMessage>> = _newChat

    private var _removeChatListener: () -> Unit = {}
    val removeChatListener: () -> Unit get() = _removeChatListener

    fun getCurrentUserInfo() = viewModelScope.launch {
        getCurUserInfoUseCase().let {
            _currentUserBasicInfo.emit(it)
        }
    }

    fun getGroupInfo(groupUId: String) = viewModelScope.launch {
        getMembersInfo(getGroupDetailUseCase(groupUId).membersUId)
    }

    fun getMembersInfo(membersUId: List<String>) = viewModelScope.launch {
        _groupMembersInfo.emit(membersUId.map { memberUId -> getUserInfoUseCase(memberUId).first })
    }

    fun getChatMembersInfo() = viewModelScope.launch {
        combine(
            _currentUserBasicInfo,
            _groupMembersInfo
        ) { currentUserBasicInfo, groupMembersInfo ->
            Pair(groupMembersInfo, currentUserBasicInfo)
        }.collectLatest {
            if (it.first.isNotEmpty() && it.second.first.isNotEmpty()) {
                _chatGroupMembersInfo.postValue(it)
            }
        }
    }

    private fun getPastMessages(groupUId: String) = viewModelScope.launch {
        getPastGroupMessageUseCase(groupUId).collectLatest {
            _chatLogs.replaceAll(it, true)
        }
    }

    fun receiveMessage(groupUId: String) = viewModelScope.launch {
        getPastMessages(groupUId).join()
        _removeChatListener = getGroupMessageUseCase(groupUId) {
            if (_chatLogs.value?.contains(it) == false) {
                _chatLogs.add(it, false)
                _newChat.value = Event(it)
            }
        }
    }

    fun sendMessage(groupUId: String, messageText: String) = viewModelScope.launch {
        val message = createMessage(groupUId, messageText)
        sendGroupMessageUseCase(message, messageListener)
    }

    private val messageListener = object : OnGrpMessageResponse {
        override fun onSuccess(message: GroupMessage) {
            // TODO Notification 발송
        }

        override fun onFailed(message: GroupMessage) { // TODO
        }
    }

    private fun createMessage(groupUId: String, messageText: String): GroupMessage =
        GroupMessage(
            createdAt = System.currentTimeMillis(),
            id = generateId(),
            groupId = groupUId,
            from = "", // 현재 접속한 유저 id
            to = arrayListOf(),
            status = arrayListOf(),
            deliveryTime = arrayListOf(),
            seenTime = arrayListOf(),
            textMessage = TextMessage(text = messageText)
        )

    private fun generateId(length: Int = 20): String { //ex: bwUIoWNCSQvPZh8xaFuz
        val alphaNumeric = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return alphaNumeric.shuffled().take(length).joinToString("")
    }
}