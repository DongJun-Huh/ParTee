package com.golfzon.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.group.CreateScreenRoomUseCase
import com.golfzon.domain.usecase.group.GetGroupDetailUseCase
import com.golfzon.domain.usecase.group.GetGroupsUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val createScreenRoomUseCase: CreateScreenRoomUseCase
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

    fun getGroups() = viewModelScope.launch {
        getGroupsUseCase()?.let {
            _groups.replaceAll(it, true)
        }
    }

    fun getGroupDetail(groupUId: String) = viewModelScope.launch {
        getGroupDetailUseCase(groupUId).let { groupDetail ->
            _curGroupDetail.postValue(Event(groupDetail))
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

    fun createScreenRoom(groupUId: String, groupInfo: GroupScreenRoomInfo) = viewModelScope.launch {
        createScreenRoomUseCase(groupUId, groupInfo).let {
            _isCreateScreenRoomSuccess.postValue(Event(it))
        }
    }
}