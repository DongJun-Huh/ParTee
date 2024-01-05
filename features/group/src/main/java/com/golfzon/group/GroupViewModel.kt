package com.golfzon.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.ChatMessageType
import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.model.TextMessage
import com.golfzon.domain.model.User
import com.golfzon.domain.repository.OnGrpMessageResponse
import com.golfzon.domain.usecase.chat.SendGroupMessageUseCase
import com.golfzon.domain.usecase.group.CreateScreenRoomUseCase
import com.golfzon.domain.usecase.group.GetGroupDetailUseCase
import com.golfzon.domain.usecase.group.GetGroupsUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val createScreenRoomUseCase: CreateScreenRoomUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase
) : ViewModel() {
    private val _groups = ListLiveData<Group>()
    val groups: ListLiveData<Group> get() = _groups

    private val _curGroupDetail = MutableLiveData<Event<Group>>()
    val curGroupDetail: LiveData<Event<Group>> get() = _curGroupDetail

    private val _curFirstTeamMembers = ListLiveData<User>()
    val curFirstTeamMembers: ListLiveData<User> get() = _curFirstTeamMembers

    private val _curSecondTeamMembers = ListLiveData<User>()
    val curSecondTeamMembers: ListLiveData<User> get() = _curSecondTeamMembers

    private val _isCreateScreenRoomSuccess = MutableLiveData<Event<Boolean>>()
    val isCreateScreenRoomSuccess get() : LiveData<Event<Boolean>> = _isCreateScreenRoomSuccess

    var createRoomScreenDateTime = MutableLiveData<LocalDateTime>(LocalDateTime.now().plusDays(7L))
    var createRoomScreenPlaceName = MutableLiveData<String>()
    var createRoomScreenPlaceUId = MutableLiveData<String>()
    var createRoomScreenPlaceRoadAddress = MutableLiveData<String>()
    var createRoomScreenPlacePastAddress = MutableLiveData<String>()
    var createRoomScreenFee = MutableLiveData<String>()

    fun getGroups() = viewModelScope.launch {
        getGroupsUseCase()?.let {
            val memberInfoAddedGroup = it.map { group ->
                group.copy(
                    membersInfo = group.membersUId.mapNotNull { memberUId ->
                        getUserInfoUseCase(memberUId).first
                    }
                )
            }
            _groups.replaceAll(memberInfoAddedGroup, true)
        }
    }

    fun getGroupDetail(groupUId: String) = viewModelScope.launch {
        getGroupDetailUseCase(groupUId).let { groupDetail ->
            val memberInfoAddedGroupDetail = groupDetail.copy(
                membersInfo = groupDetail.membersUId.mapNotNull { memberUId ->
                    getUserInfoUseCase(memberUId).first
                }
            )
            _curGroupDetail.postValue(Event(memberInfoAddedGroupDetail))
        }
    }

    fun getTeamMembersInfo(firstTeamMembersUId: List<String>, secondTeamMembersUId: List<String>) =
        viewModelScope.launch {
            _curFirstTeamMembers.replaceAll(
                firstTeamMembersUId.map { getUserInfoUseCase(it).first },
                true
            )
            curSecondTeamMembers.replaceAll(
                secondTeamMembersUId.map { getUserInfoUseCase(it).first },
                true
            )
        }

    fun createScreenRoom(groupUId: String) = viewModelScope.launch {
        val reservationGroupInfo = GroupScreenRoomInfo(
            screenRoomUId = groupUId,
            screenRoomPlaceName = createRoomScreenPlaceName.value ?: "",
            screenRoomPlaceUId = createRoomScreenPlaceUId.value ?: "",
            screenRoomDateTime = createRoomScreenDateTime.value ?: LocalDateTime.now(),
            screenRoomPlaceRoadAddress = createRoomScreenPlaceRoadAddress.value ?: "",
            screenRoomPlacePastAddress = createRoomScreenPlacePastAddress.value ?: ""
        )
        createScreenRoomUseCase(groupUId, reservationGroupInfo).let {
            _isCreateScreenRoomSuccess.postValue(Event(it))
        }
    }

    fun sendMessageWithReservation(groupUId: String) = viewModelScope.launch {
        val message = createMessageWithReservation(groupUId)
        sendGroupMessageUseCase(message, messageListener)
    }

    private val messageListener = object : OnGrpMessageResponse {
        override fun onSuccess(message: GroupMessage) {
            // TODO Notification 발송
        }

        override fun onFailed(message: GroupMessage) { // TODO
        }
    }

    private fun generateId(length: Int = 20): String { //ex: bwUIoWNCSQvPZh8xaFuz
        val alphaNumeric = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return alphaNumeric.shuffled().take(length).joinToString("")
    }

    private fun createMessageWithReservation(
        groupUId: String
    ): GroupMessage =
        GroupMessage(
            createdAt = System.currentTimeMillis(),
            id = generateId(),
            groupId = groupUId,
            from = "", // 현재 접속한 유저 id
            to = arrayListOf(),
            status = arrayListOf(),
            deliveryTime = arrayListOf(),
            seenTime = arrayListOf(),
            type = ChatMessageType.RESERVATION,
            textMessage = TextMessage(
                text = "${
                    (createRoomScreenDateTime.value ?: LocalDateTime.now()).format(
                        DateTimeFormatter.ofPattern("yy년 MM월 dd일 EEEE a hh:mm")
                    )
                }\n${createRoomScreenPlaceName.value ?: ""}"
            ),
            placeUId = createRoomScreenPlaceUId.value ?: ""
        )
}