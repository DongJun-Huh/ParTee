package com.golfzon.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetSearchedUsersUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoBriefUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import com.golfzon.domain.usecase.team.RequestTeamOrganizedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getUserTeamInfoBriefUseCase: GetUserTeamInfoBriefUseCase,
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
    private val getSearchedUsersUseCase: GetSearchedUsersUseCase,
    private val requestTeamOrganizedUseCase: RequestTeamOrganizedUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {
    private val _teamInfoBrief = MutableLiveData<TeamInfo>()
    val teamInfoBrief: LiveData<TeamInfo> get() = _teamInfoBrief

    private val _teamInfoDetail = MutableLiveData<Team?>()
    val teamInfoDetail: LiveData<Team?> get() = _teamInfoDetail

    private val _searchedUsers = ListLiveData<User>()
    val searchedUsers: ListLiveData<User> get() = _searchedUsers

    private val _teamUsers = ListLiveData<Pair<User, Boolean>>()
    val teamUsers: ListLiveData<Pair<User, Boolean>> get() = _teamUsers

    private val _newTeam = MutableLiveData<Team>()
    val newTeam: LiveData<Team> get() = _newTeam

    fun getTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let { curTeam ->
            _teamInfoDetail.postValue(curTeam)
        }
    }

    fun getNewTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let {
            _newTeam.postValue(it)
        }
    }

    fun clearUserInfo() {
        _teamUsers.clear(true)
    }

    fun getTeamMemberInfo(UId: String) = viewModelScope.launch {
        getUserInfoUseCase(UId).let {
            _teamUsers.add(it, true)
        }
    }

    fun searchUsers(nickname: String) = viewModelScope.launch {
        getSearchedUsersUseCase(nickname = nickname).let {
            val results = it.filter { user ->
                !(_newTeam.value?.membersUId?.contains(user.userUId) ?: true)
            }
            _searchedUsers.replaceAll(results, true)
        }
    }

    fun clearSearchedUsers() {
        _searchedUsers.clear(true)
    }

    fun organizeTeam() = viewModelScope.launch {
        requestTeamOrganizedUseCase(
            // TODO teamName, teamImageUrl, leaderUId, membersUId, headCount, searchingTimes, searchingLocations, openChatUrl만 설정
            newTeam = _newTeam.value!!
        )
    }

    fun addTeamMember(newUserUId: String) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    membersUId = it.membersUId + listOf(newUserUId),
                    headCount = it.headCount + 1
                )
            }
        )
    }

    fun setLocation(locations: List<String>) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    searchingLocations = locations
                )
            }
        )
    }

    fun changeTeamName(newTeamName: String) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    teamName = newTeamName
                )
            }
        )
    }
}